package com.hartwig.actin.clinical

import com.fasterxml.jackson.databind.ObjectMapper
import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.clinical.curation.CURATION_DIRECTORY
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationDoidValidator
import com.hartwig.actin.clinical.curation.TestAtcFactory
import com.hartwig.actin.clinical.curation.TestDrugInteractionsFactory
import com.hartwig.actin.clinical.curation.TestQtProlongatingFactory
import com.hartwig.actin.clinical.feed.emc.ClinicalFeedReader
import com.hartwig.actin.clinical.feed.emc.EmcClinicalFeedIngestor
import com.hartwig.actin.clinical.feed.emc.FEED_DIRECTORY
import com.hartwig.actin.clinical.feed.emc.FeedModel
import com.hartwig.actin.datamodel.clinical.ingestion.FeedValidationWarning
import com.hartwig.actin.datamodel.clinical.ingestion.QuestionnaireCurationError
import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireVersion
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.ingestion.IngestionResult
import com.hartwig.actin.datamodel.clinical.ingestion.PatientIngestionResult
import com.hartwig.actin.datamodel.clinical.ingestion.UnusedCurationConfig
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.doid.config.DoidManualConfig
import com.hartwig.actin.icd.TestIcdFactory
import com.hartwig.actin.testutil.ResourceLocator.resourceOnClasspath
import com.hartwig.actin.util.json.GsonSerializer
import java.io.File
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.Before
import org.junit.Test

private const val PATIENT = "ACTN01029999"
private val EXPECTED_CLINICAL_RECORD = "${resourceOnClasspath("clinical_record")}/$PATIENT.clinical.json"

class ClinicalIngestionFeedAdapterTest {
    lateinit var curationDatabase: CurationDatabaseContext
    private lateinit var adapter: ClinicalIngestionFeedAdapter

    @Before
    fun setup() {
        val testDoidModel = TestDoidModelFactory.createWithDoidManualConfig(
            DoidManualConfig(
                emptySet(),
                emptySet(),
                mapOf(
                    "2513" to CurationDoidValidator.DISEASE_OF_CELLULAR_PROLIFERATION_DOID,
                    "299" to CurationDoidValidator.DISEASE_OF_CELLULAR_PROLIFERATION_DOID,
                    "3908" to CurationDoidValidator.DISEASE_OF_CELLULAR_PROLIFERATION_DOID,
                    "10286" to CurationDoidValidator.DISEASE_OF_CELLULAR_PROLIFERATION_DOID,
                    "0050933" to CurationDoidValidator.DISEASE_OF_CELLULAR_PROLIFERATION_DOID,
                    "5082" to CurationDoidValidator.DISEASE_DOID,
                    "11335" to CurationDoidValidator.DISEASE_DOID,
                    "0060500" to CurationDoidValidator.DISEASE_DOID,
                    "0081062" to CurationDoidValidator.DISEASE_DOID,
                    "0040046" to CurationDoidValidator.DISEASE_DOID
                ),
                emptySet()
            )
        )

        curationDatabase = CurationDatabaseContext.create(
            CURATION_DIRECTORY,
            CurationDoidValidator(testDoidModel),
            TestIcdFactory.createTestModel(),
            TestTreatmentDatabaseFactory.createProper()
        )
        adapter = ClinicalIngestionFeedAdapter(
            EmcClinicalFeedIngestor.create(
                FEED_DIRECTORY,
                CURATION_DIRECTORY,
                curationDatabase,
                TestAtcFactory.createProperAtcModel(),
                TestDrugInteractionsFactory.createProper(),
                TestQtProlongatingFactory.createProper(),
                testDoidModel,
                TestTreatmentDatabaseFactory.createProper()
            ), curationDatabase
        )
    }

    @Test
    fun `Output should not have changed`() {
        val jsonMapper = ObjectMapper()
        val ingestionResult = adapter.run().second
        assertThat(jsonMapper.readTree(ClinicalRecordJson.toJson(ingestionResult.first())))
            .isEqualTo(jsonMapper.readTree(File(EXPECTED_CLINICAL_RECORD).readText()))
    }

    @Test
    fun `Should run ingestion from proper curation and feed files, read from filesystem`() {
        assertQuestionnaireInFeedIsOfLatestVersion()

        val validationErrors = curationDatabase.validate()
        assertThat(validationErrors).isEmpty()

        val (ingestionResult, clinicalRecords) = adapter.run()
        assertThat(ingestionResult).isNotNull
        val patientResults = ingestionResult.patientResults
        assertThat(patientResults).hasSize(1)
        assertThat(patientResults[0].patientId).isEqualTo(PATIENT)
        assertThat(patientResults[0].curationResults).isEmpty()
        assertThat(patientResults[0].questionnaireCurationErrors)
            .containsExactly(QuestionnaireCurationError(PATIENT, "Unrecognized questionnaire option: 'Probbly'"))
        assertThat(patientResults[0].feedValidationWarnings).containsExactly(
            FeedValidationWarning(
                PATIENT,
                "Empty vital function value"
            )
        )
        assertThat(clinicalRecords.first()).isEqualTo(ClinicalRecordJson.read(EXPECTED_CLINICAL_RECORD))
        assertThat(clinicalRecords.first().surgeries.size).isEqualTo(1)

        assertThat(ingestionResult.unusedConfigs).containsExactlyInAnyOrder(
            UnusedCurationConfig(category = CurationCategory.ONCOLOGICAL_HISTORY, input = "capecitabine and oxi"),
            UnusedCurationConfig(category = CurationCategory.PRIMARY_TUMOR, input = "long | metastase adenocarcinoom"),
            UnusedCurationConfig(category = CurationCategory.PRIMARY_TUMOR, input = "carcinoma | unknown"),
            UnusedCurationConfig(category = CurationCategory.COMORBIDITY, input = "pijn bij maligne neoplasma van longen"),
            UnusedCurationConfig(category = CurationCategory.COMORBIDITY, input = "sarcoidose"),
            UnusedCurationConfig(category = CurationCategory.COMORBIDITY, input = "overige"),
            UnusedCurationConfig(category = CurationCategory.LESION_LOCATION, input = "brain"),
            UnusedCurationConfig(category = CurationCategory.COMORBIDITY, input = "dysphagia"),
            UnusedCurationConfig(category = CurationCategory.COMORBIDITY, input = "neuropathy gr3"),
            UnusedCurationConfig(category = CurationCategory.MOLECULAR_TEST_IHC, input = "immunohistochemie erbb2 3+"),
            UnusedCurationConfig(category = CurationCategory.MOLECULAR_TEST_PDL1, input = "cps pd l1 > 20"),
            UnusedCurationConfig(category = CurationCategory.DOSAGE_UNIT_TRANSLATION, input = "stuk"),
            UnusedCurationConfig(category = CurationCategory.SEQUENCING_TEST, input = "kras g12f"),
            UnusedCurationConfig(category = CurationCategory.SURGERY_NAME, input = "surgery1"),
            UnusedCurationConfig(category = CurationCategory.LESION_LOCATION, input = "and possibly lymph nodes"),
            UnusedCurationConfig(category = CurationCategory.COMORBIDITY, input = "morfine"),
            UnusedCurationConfig(category = CurationCategory.COMORBIDITY, input = "nikkel"),
            UnusedCurationConfig(category = CurationCategory.TOXICITY_TRANSLATION, input = "Nausea"),
            UnusedCurationConfig(category = CurationCategory.LAB_MEASUREMENT, input = "hb | hemoglobine"),
            UnusedCurationConfig(category = CurationCategory.LAB_MEASUREMENT, input = "plt | trombocyten"),
            UnusedCurationConfig(category = CurationCategory.LAB_MEASUREMENT, input = "dc_lymfo | lymfocyten"),
            UnusedCurationConfig(category = CurationCategory.LAB_MEASUREMENT, input = "bg_o2sgem | o2-saturatie gemeten")
        )

        val gson = GsonSerializer.create()
        val serialized = gson.toJson(ingestionResult).toByteArray()

        val deserialized = gson.fromJson(serialized.decodeToString(), IngestionResult::class.java)
        // The clinical record is not serialized, so we need to compare the patient results separately:
        assertThat(deserialized.copy(patientResults = emptyList())).isEqualTo(ingestionResult.copy(patientResults = emptyList()))

        val patientIngestionResult = ingestionResult.patientResults.first()
        assertThat(deserialized.patientResults.size).isEqualTo(1)
        assertThat(deserialized.patientResults).extracting(
            PatientIngestionResult::patientId,
            PatientIngestionResult::status,
            PatientIngestionResult::curationResults,
            PatientIngestionResult::questionnaireCurationErrors,
            PatientIngestionResult::feedValidationWarnings
        )
            .containsExactly(
                tuple(
                    patientIngestionResult.patientId,
                    patientIngestionResult.status,
                    patientIngestionResult.curationResults,
                    patientIngestionResult.questionnaireCurationErrors,
                    patientIngestionResult.feedValidationWarnings
                )
            )
    }

    private fun assertQuestionnaireInFeedIsOfLatestVersion() {
        val feed = FeedModel(
            ClinicalFeedReader.read(FEED_DIRECTORY)
        )
        assertThat(feed.read().size).isEqualTo(1)
        val versionUnderTest = QuestionnaireVersion.version(feed.read()[0].questionnaireEntries.maxByOrNull { it.authored }!!)
        val latestVersion = QuestionnaireVersion.entries.last()

        assertThat(versionUnderTest).isNotNull()
        assertThat(versionUnderTest).isEqualTo(latestVersion)
    }
}
