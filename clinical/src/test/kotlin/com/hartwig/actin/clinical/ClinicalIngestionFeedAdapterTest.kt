package com.hartwig.actin.clinical

import com.google.common.io.Resources
import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.clinical.curation.CURATION_DIRECTORY
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationDoidValidator
import com.hartwig.actin.clinical.curation.TestAtcFactory
import com.hartwig.actin.clinical.feed.emc.EmcClinicalFeedIngestor
import com.hartwig.actin.clinical.feed.emc.FEED_DIRECTORY
import com.hartwig.actin.clinical.feed.emc.FeedValidationWarning
import com.hartwig.actin.clinical.feed.emc.questionnaire.QuestionnaireCurationError
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.doid.config.DoidManualConfig
import com.hartwig.actin.util.json.GsonSerializer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.Test

private const val PATIENT = "ACTN01029999"
val EXPECTED_CLINICAL_RECORD: String =
    "${Resources.getResource("clinical_record").path}/$PATIENT.clinical.json"

class ClinicalIngestionFeedAdapterTest {

    @Test
    fun `Should run ingestion from proper curation and feed files, read from filesystem`() {
        val curationDatabase = CurationDatabaseContext.create(
            CURATION_DIRECTORY,
            CurationDoidValidator(
                TestDoidModelFactory.createWithDoidManualConfig(
                    DoidManualConfig(
                        emptySet(),
                        emptySet(),
                        mapOf(
                            "2513" to CurationDoidValidator.DISEASE_OF_CELLULAR_PROLIFERATION_DOID,
                            "299" to CurationDoidValidator.DISEASE_OF_CELLULAR_PROLIFERATION_DOID,
                            "3908" to CurationDoidValidator.DISEASE_OF_CELLULAR_PROLIFERATION_DOID,
                            "5082" to CurationDoidValidator.DISEASE_DOID,
                            "11335" to CurationDoidValidator.DISEASE_DOID,
                            "0060500" to CurationDoidValidator.DISEASE_DOID
                        )
                    )
                )
            ),
            TestTreatmentDatabaseFactory.createProper()
        )
        val ingestion = ClinicalIngestionFeedAdapter(
            EmcClinicalFeedIngestor.create(
                FEED_DIRECTORY,
                CURATION_DIRECTORY,
                curationDatabase,
                TestAtcFactory.createProperAtcModel()
            ), curationDatabase
        )

        val validationErrors = curationDatabase.validate()
        assertThat(validationErrors).isEmpty()

        val ingestionResult = ingestion.run()
        assertThat(ingestionResult).isNotNull
        val patientResults = ingestionResult.patientResults
        assertThat(patientResults[0].status).isEqualTo(PatientIngestionStatus.PASS)
        assertThat(patientResults).hasSize(1)
        assertThat(patientResults[0].patientId).isEqualTo(PATIENT)
        assertThat(patientResults[0].curationResults).isEmpty()
        assertThat(patientResults[0].clinicalRecord).isEqualTo(ClinicalRecordJson.read(EXPECTED_CLINICAL_RECORD))
        assertThat(patientResults[0].questionnaireCurationErrors)
            .containsExactly(QuestionnaireCurationError(PATIENT, "Unrecognized questionnaire option: 'Probably'"))
        assertThat(patientResults[0].feedValidationWarnings).containsExactly(
            FeedValidationWarning(
                PATIENT,
                "Empty vital function value"
            )
        )

        assertThat(ingestionResult.unusedConfigs).containsExactlyInAnyOrder(
            UnusedCurationConfig(categoryName = "Oncological History", input = "capecitabine and oxi"),
            UnusedCurationConfig(categoryName = "Primary Tumor", input = "long | metastase adenocarcinoom"),
            UnusedCurationConfig(categoryName = "Non Oncological History", input = "pijn bij maligne neoplasma van longen"),
            UnusedCurationConfig(categoryName = "Complication", input = "overige"),
            UnusedCurationConfig(categoryName = "Lesion Location", input = "brain"),
            UnusedCurationConfig(categoryName = "Toxicity", input = "dysphagia"),
            UnusedCurationConfig(categoryName = "Molecular Test IHC", input = "immunohistochemie erbb2 3+"),
            UnusedCurationConfig(categoryName = "Molecular Test PDL1", input = "cps pd l1 > 20"),
            UnusedCurationConfig(categoryName = "Dosage Unit Translation", input = "stuk")
        )

        val gson = GsonSerializer.create()
        val serialized = gson.toJson(ingestionResult).toByteArray()

        val deserialized = gson.fromJson(serialized.decodeToString(), IngestionResult::class.java)
        // The clinical record is not serialized, so we need to compare the patient results separately:
        assertThat(deserialized.copy(patientResults = emptyList())).isEqualTo(ingestionResult.copy(patientResults = emptyList()))

        val patientIngestionResult = ingestionResult.patientResults.first()
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
}