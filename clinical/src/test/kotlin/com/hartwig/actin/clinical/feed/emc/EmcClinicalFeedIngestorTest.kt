package com.hartwig.actin.clinical.feed.emc

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.emc.TestFeedFactory.createTestClinicalFeed
import com.hartwig.actin.datamodel.clinical.ClinicalStatus
import com.hartwig.actin.datamodel.clinical.Comorbidity
import com.hartwig.actin.datamodel.clinical.TumorDetails
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EmcClinicalFeedIngestorTest {

    @Test
    fun `Should add feed validation warning for missing questionnaires`() {
        val emcCliniacalFeed = createTestClinicalFeed().copy(questionnaireEntries = emptyList())
        val feed = FeedModel(emcCliniacalFeed)

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