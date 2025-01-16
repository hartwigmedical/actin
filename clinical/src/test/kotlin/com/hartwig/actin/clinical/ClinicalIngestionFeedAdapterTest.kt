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
import com.hartwig.actin.clinical.feed.emc.FeedValidationWarning
import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireCurationError
import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireVersion
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
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
        val ingestionResult = adapter.run()
        assertThat(
            jsonMapper.readTree(
                ClinicalRecordJson.toJson(
                    ingestionResult.patientResults[0].clinicalRecord
                )
            )
        ).isEqualTo(jsonMapper.readTree(File(EXPECTED_CLINICAL_RECORD).readText()))
    }

    @Test
    fun `Should run ingestion from proper curation and feed files, read from filesystem`() {
        assertQuestionnaireInFeedIsOfLatestVersion()

        val validationErrors = curationDatabase.validate()
        assertThat(validationErrors).isEmpty()

        val ingestionResult = adapter.run()
        assertThat(ingestionResult).isNotNull
        val patientResults = ingestionResult.patientResults
        assertThat(patientResults).hasSize(1)
        assertThat(patientResults[0].patientId).isEqualTo(PATIENT)
        assertThat(patientResults[0].curationResults).isEmpty()
        assertThat(patientResults[0].clinicalRecord).isEqualTo(ClinicalRecordJson.read(EXPECTED_CLINICAL_RECORD))
        assertThat(patientResults[0].clinicalRecord.surgeries.size).isEqualTo(1)
        assertThat(patientResults[0].questionnaireCurationErrors)
            .containsExactly(QuestionnaireCurationError(PATIENT, "Unrecognized questionnaire option: 'Probbly'"))
        assertThat(patientResults[0].feedValidationWarnings).containsExactly(
            FeedValidationWarning(
                PATIENT,
                "Empty vital function value"
            )
        )

        assertThat(ingestionResult.unusedConfigs).containsExactlyInAnyOrder(
            UnusedCurationConfig(categoryName = "Oncological History", input = "capecitabine and oxi"),
            UnusedCurationConfig(categoryName = "Primary Tumor", input = "long | metastase adenocarcinoom"),
            UnusedCurationConfig(categoryName = "Primary Tumor", input = "carcinoma | unknown"),
            UnusedCurationConfig(categoryName = "Comorbidity", input = "pijn bij maligne neoplasma van longen"),
            UnusedCurationConfig(categoryName = "Comorbidity", input = "sarcoidose"),
            UnusedCurationConfig(categoryName = "Comorbidity", input = "overige"),
            UnusedCurationConfig(categoryName = "Lesion Location", input = "brain"),
            UnusedCurationConfig(categoryName = "Comorbidity", input = "dysphagia"),
            UnusedCurationConfig(categoryName = "Comorbidity", input = "neuropathy gr3"),
            UnusedCurationConfig(categoryName = "Molecular Test IHC", input = "immunohistochemie erbb2 3+"),
            UnusedCurationConfig(categoryName = "Molecular Test PDL1", input = "cps pd l1 > 20"),
            UnusedCurationConfig(categoryName = "Dosage Unit Translation", input = "stuk"),
            UnusedCurationConfig(categoryName = "Sequencing Test", input = "kras g12f"),
            UnusedCurationConfig(categoryName = "Surgery Name", input = "surgery1"),
            UnusedCurationConfig(categoryName = "Lesion Location", input = "and possibly lymph nodes"),
            UnusedCurationConfig(categoryName = "Comorbidity", input = "morfine"),
            UnusedCurationConfig(categoryName = "Comorbidity", input = "nikkel")
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
            PatientIngestionResult::clinicalRecord,
            PatientIngestionResult::curationResults,
            PatientIngestionResult::questionnaireCurationErrors,
            PatientIngestionResult::feedValidationWarnings
        )
            .containsExactly(
                tuple(
                    patientIngestionResult.patientId,
                    patientIngestionResult.status,
                    null,
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
        val versionUnderTest = QuestionnaireVersion.version(feed.read()[0].latestQuestionnaireEntry!!)
        val latestVersion = QuestionnaireVersion.entries.last()

        assertThat(versionUnderTest).isNotNull()
        assertThat(versionUnderTest).isEqualTo(latestVersion)
    }
}
