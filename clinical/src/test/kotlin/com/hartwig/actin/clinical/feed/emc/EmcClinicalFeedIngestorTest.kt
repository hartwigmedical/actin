package com.hartwig.actin.clinical.feed.emc

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.emc.TestFeedFactory.createTestClinicalFeed
import com.hartwig.actin.clinical.feed.emc.patient.PatientEntry
import com.hartwig.actin.datamodel.clinical.ClinicalStatus
import com.hartwig.actin.datamodel.clinical.Comorbidity
import com.hartwig.actin.datamodel.clinical.Gender
import com.hartwig.actin.datamodel.clinical.TumorDetails
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class EmcClinicalFeedIngestorTest {

    @Test
    fun `Should add feed validation warning for missing questionnaires`() {
        val emcClinicalFeed = createTestClinicalFeed().copy(
            questionnaireEntries = emptyList(),
            patientEntries = listOf(PatientEntry("", 1990, Gender.FEMALE, LocalDate.now(), null))
        )
        val feed = FeedModel(emcClinicalFeed)

        val emcClinicalFeedIngestor = EmcClinicalFeedIngestor(
            feed,
            mockk { every { extract(any(), any()) }.returns(ExtractionResult(TumorDetails(), CurationExtractionEvaluation())) },
            mockk {
                every { extract(any(), any(), any(), any()) }.returns(
                    ExtractionResult(
                        emptyList<Comorbidity>() to ClinicalStatus(),
                        CurationExtractionEvaluation()
                    )
                )
            },
            mockk { every { extract(any(), any()) }.returns(ExtractionResult(emptyList(), CurationExtractionEvaluation())) },
            mockk { every { extract(any(), any()) }.returns(ExtractionResult(emptyList(), CurationExtractionEvaluation())) },
            mockk { every { extract(any(), any()) }.returns(ExtractionResult(emptyList(), CurationExtractionEvaluation())) },
            mockk { every { extract(any(), any()) }.returns(ExtractionResult(emptyList(), CurationExtractionEvaluation())) },
            mockk { every { extract(any(), any()) }.returns(ExtractionResult(emptyList(), CurationExtractionEvaluation())) },
            mockk { every { extract(any(), any()) }.returns(ExtractionResult(emptyList(), CurationExtractionEvaluation())) },
            mockk { every { extract(any(), any()) }.returns(ExtractionResult(emptyList(), CurationExtractionEvaluation())) },
        )

        val (_, patientIngestionResult, _) = emcClinicalFeedIngestor.ingest().first()
        assertThat(patientIngestionResult.feedValidationWarnings.map { it.message }).contains("No Questionnaire found")

    }
}