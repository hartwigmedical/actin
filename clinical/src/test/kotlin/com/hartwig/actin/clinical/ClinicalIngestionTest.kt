package com.hartwig.actin.clinical

import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.clinical.curation.extraction.BloodTransfusionsExtractor
import com.hartwig.actin.clinical.curation.extraction.ClinicalStatusExtractor
import com.hartwig.actin.clinical.curation.extraction.ComplicationsExtractor
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.curation.extraction.IntoleranceExtractor
import com.hartwig.actin.clinical.curation.extraction.LabValueExtractor
import com.hartwig.actin.clinical.curation.extraction.MedicationExtractor
import com.hartwig.actin.clinical.curation.extraction.PriorMolecularTestsExtractor
import com.hartwig.actin.clinical.curation.extraction.PriorOtherConditionsExtractor
import com.hartwig.actin.clinical.curation.extraction.PriorSecondPrimaryExtractor
import com.hartwig.actin.clinical.curation.extraction.ToxicityExtractor
import com.hartwig.actin.clinical.curation.extraction.TreatmentHistoryExtractor
import com.hartwig.actin.clinical.curation.extraction.TumorDetailsExtractor
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.clinical.feed.FeedModel
import com.hartwig.actin.clinical.feed.TestFeedFactory
import com.hartwig.actin.clinical.feed.questionnaire.Questionnaire
import com.hartwig.actin.clinical.feed.questionnaire.TestQuestionnaireFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ClinicalIngestionTest {

    @Test
    fun `Should ingest data with no warnings and with questionnaire with OK status`() {
        val tumorDetailsExtractor = mockk<TumorDetailsExtractor>()
        val questionnaireSlot = slot<Questionnaire>()
        every {
            tumorDetailsExtractor.extract(
                PatientId.from(TestFeedFactory.TEST_SUBJECT),
                capture(questionnaireSlot)
            )
        } returns ExtractionResult(
            TestClinicalFactory.createProperTestClinicalRecord().tumor(),
            ExtractionEvaluation(emptySet(), emptySet())
        )
        val complicationsExtractor = mockk<ComplicationsExtractor>()
        val clinicalStatusExtractor = mockk<ClinicalStatusExtractor>()
        val treatmentHistoryExtractor = mockk<TreatmentHistoryExtractor>()
        val priorSecondPrimaryExtractor = mockk<PriorSecondPrimaryExtractor>()
        val priorOtherConditionExtractor = mockk<PriorOtherConditionsExtractor>()
        val priorMolecularTestsExtractor = mockk<PriorMolecularTestsExtractor>()
        val labValueExtractor = mockk<LabValueExtractor>()
        val toxicityExtractor = mockk<ToxicityExtractor>()
        val intoleranceExtractor = mockk<IntoleranceExtractor>()
        val medicationExtractor = mockk<MedicationExtractor>()
        val bloodTransfusionsExtractor = mockk<BloodTransfusionsExtractor>()
        val ingestion = ClinicalIngestion(
            TestFeedFactory.createProperTestFeedModel(),
            tumorDetailsExtractor,
            complicationsExtractor,
            clinicalStatusExtractor,
            treatmentHistoryExtractor,
            priorSecondPrimaryExtractor,
            priorOtherConditionExtractor,
            priorMolecularTestsExtractor,
            labValueExtractor,
            toxicityExtractor,
            intoleranceExtractor,
            medicationExtractor,
            bloodTransfusionsExtractor
        )
        val ingestionResults = ingestion.run()
        assertThat(ingestionResults)
    }
}