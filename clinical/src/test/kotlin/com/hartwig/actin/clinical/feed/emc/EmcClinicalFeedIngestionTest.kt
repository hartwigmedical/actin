package com.hartwig.actin.clinical.feed.emc

import com.fasterxml.jackson.databind.ObjectMapper
import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.clinical.curation.CURATION_DIRECTORY
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationDoidValidator
import com.hartwig.actin.clinical.curation.TestAtcFactory
import com.hartwig.actin.clinical.curation.TestDrugInteractionsFactory
import com.hartwig.actin.clinical.curation.TestQtProlongatingFactory
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.datamodel.clinical.ingestion.FeedValidationWarning
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.doid.config.DoidManualConfig
import com.hartwig.actin.icd.TestIcdFactory
import com.hartwig.actin.testutil.ResourceLocator.resourceOnClasspath
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.io.File
import java.time.LocalDate

private const val PATIENT = "ACTN01029999"
val EMC_FEED_DIRECTORY = resourceOnClasspath("feed/emc/input")
val EMC_EXPECTED_CLINICAL_RECORD = resourceOnClasspath("feed/emc/output/ACTN01029999.clinical.json")

class EmcClinicalFeedIngestionTest {

    lateinit var curationDatabase: CurationDatabaseContext
    lateinit var feedIngestion: EmcClinicalFeedIngestion
    private val jsonMapper = ObjectMapper()

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

        feedIngestion = EmcClinicalFeedIngestion.create(
            EMC_FEED_DIRECTORY,
            curationDatabase,
            TestAtcFactory.createProperAtcModel(),
            TestDrugInteractionsFactory.createProper(),
            TestQtProlongatingFactory.createProper(),
            testDoidModel,
            TestTreatmentDatabaseFactory.createProper()
        )

    }

    @Test
    fun `Should load feed data from json and convert to clinical record`() {
        val validationErrors = curationDatabase.validate()
        assertThat(validationErrors).isEmpty()

        val results = feedIngestion.ingest()
        assertThat(results).hasSize(1)

        val (clinicalRecord, ingestionResult, _) = results.first()

        assertThat(ingestionResult.patientId).isEqualTo(PATIENT)
        assertThat(ingestionResult.registrationDate).isEqualTo(LocalDate.of(2020, 7, 13))
        assertThat(ingestionResult.curationResults).isEmpty()
        assertThat(ingestionResult.feedValidationWarnings).containsExactly(
            FeedValidationWarning(
                PATIENT,
                "Empty vital function value"
            )
        )
        assertThat(clinicalRecord).isEqualTo(ClinicalRecordJson.read(EMC_EXPECTED_CLINICAL_RECORD))

        assertThat(jsonMapper.readTree(ClinicalRecordJson.toJson(clinicalRecord)))
            .isEqualTo(jsonMapper.readTree(File(EMC_EXPECTED_CLINICAL_RECORD).readText()))
    }
}