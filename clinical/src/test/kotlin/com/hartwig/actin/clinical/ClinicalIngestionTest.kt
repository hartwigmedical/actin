package com.hartwig.actin.clinical

import com.google.common.io.Resources
import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.clinical.correction.QuestionnaireCorrection
import com.hartwig.actin.clinical.correction.QuestionnaireRawEntryMapper
import com.hartwig.actin.clinical.curation.CURATION_DIRECTORY
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationDoidValidator
import com.hartwig.actin.clinical.curation.TestAtcFactory
import com.hartwig.actin.clinical.feed.ClinicalFeedReader
import com.hartwig.actin.clinical.feed.FEED_DIRECTORY
import com.hartwig.actin.clinical.feed.FeedModel
import com.hartwig.actin.clinical.feed.FeedValidationWarning
import com.hartwig.actin.clinical.feed.questionnaire.QuestionnaireCurationError
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.doid.config.DoidManualConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

val EXPECTED_CLINICAL_RECORD: String =
    "${Resources.getResource("clinical_record").path}/ACTN01029999.clinical.json"

class ClinicalIngestionTest {

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
                            "5082" to CurationDoidValidator.DISEASE_DOID,
                            "11335" to CurationDoidValidator.DISEASE_DOID,
                            "0060500" to CurationDoidValidator.DISEASE_DOID
                        )
                    )
                )
            ),
            TestTreatmentDatabaseFactory.createProper()
        )
        val clinicalFeed = ClinicalFeedReader.read(FEED_DIRECTORY)
        val ingestion = ClinicalIngestion.create(
            FeedModel(
                clinicalFeed.copy(
                    questionnaireEntries = QuestionnaireCorrection.correctQuestionnaires(
                        clinicalFeed.questionnaireEntries, QuestionnaireRawEntryMapper.createFromCurationDirectory(CURATION_DIRECTORY)
                    )
                )
            ),
            curationDatabase,
            TestAtcFactory.createProperAtcModel()
        )

        val validationErrors = curationDatabase.validate()
        assertThat(validationErrors).isEmpty()

        val ingestionResult = ingestion.run()
        assertThat(ingestionResult).isNotNull
        val patientResults = ingestionResult.patientResults
        assertThat(patientResults[0].status).isEqualTo(PatientIngestionStatus.PASS)
        assertThat(patientResults).hasSize(1)
        assertThat(patientResults[0].patientId).isEqualTo("ACTN01029999")
        assertThat(patientResults[0].curationResults).isEmpty()
        assertThat(patientResults[0].clinicalRecord).isEqualTo(ClinicalRecordJson.read(EXPECTED_CLINICAL_RECORD))
        assertThat(patientResults[0].questionnaireCurationErrors)
            .containsExactly(QuestionnaireCurationError("ACTN-01-02-9999", "Unrecognized questionnaire option: 'Probably'"))
        assertThat(patientResults[0].feedValidationWarnings).containsExactly(
            FeedValidationWarning(
                "ACTN-01-02-9999",
                "Empty vital function value"
            )
        )

        assertThat(ingestionResult.unusedConfigs).containsExactly(
            UnusedCurationConfig(categoryName = "Molecular Test", input = "ihc erbb2 3+"),
            UnusedCurationConfig(categoryName = "Molecular Test", input = "cps pd l1 > 20"),
            UnusedCurationConfig(categoryName = "Dosage Unit Translation", input = "stuk")
        )
    }
}