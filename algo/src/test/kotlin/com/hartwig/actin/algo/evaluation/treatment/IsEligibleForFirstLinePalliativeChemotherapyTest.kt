package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.tumor.HasMetastaticCancer
import com.hartwig.actin.algo.evaluation.tumor.TumorTestFactory
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class IsEligibleForFirstLinePalliativeChemotherapyTest {

    private val patientRecord = TumorTestFactory.withTumorStage(null)
    private val alwaysPassMetastaticCancerEvaluation = mockk<HasMetastaticCancer> {
        every { evaluate(any()) } returns EvaluationFactory.pass("metastatic cancer")
    }

    @Test
    fun `Should fail when no metastatic cancer`() {
        val alwaysFailsMetastaticCancerEvaluation = mockk<HasMetastaticCancer> {
            every { evaluate(any()) } returns EvaluationFactory.fail("no metastatic cancer")
        }
        val function = IsEligibleForFirstLinePalliativeChemotherapy(alwaysFailsMetastaticCancerEvaluation)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(patientRecord))
    }

    @Test
    fun `Should fail when previous palliative chemotherapy`() {
        val function = IsEligibleForFirstLinePalliativeChemotherapy(alwaysPassMetastaticCancerEvaluation)
        val treatment = TreatmentTestFactory.drugTreatment("drug therapy", TreatmentCategory.CHEMOTHERAPY)
        val patientRecord =
            TreatmentTestFactory.withTreatmentHistoryEntry(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(treatment),
                    intents = setOf(Intent.PALLIATIVE)
                )
            )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(patientRecord))
    }

    @Test
    fun `Should be undetermined when patient has metastatic cancer and previous palliative targeted therapy`() {
        val treatment = TreatmentTestFactory.drugTreatment("drug therapy", TreatmentCategory.TARGETED_THERAPY)
        val function = IsEligibleForFirstLinePalliativeChemotherapy(alwaysPassMetastaticCancerEvaluation)
        val patientRecord =
            TreatmentTestFactory.withTreatmentHistoryEntry(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(treatment),
                    intents = setOf(Intent.PALLIATIVE)
                )
            )

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(patientRecord))
    }

    @Test
    fun `Should be undetermined when patient has metastatic cancer and no previous palliative therapy`() {
        val function = IsEligibleForFirstLinePalliativeChemotherapy(alwaysPassMetastaticCancerEvaluation)
        val treatment = TreatmentTestFactory.drugTreatment("drug therapy", TreatmentCategory.CHEMOTHERAPY)
        val patientRecord =
            TreatmentTestFactory.withTreatmentHistoryEntry(
                TreatmentTestFactory.treatmentHistoryEntry(
                    setOf(treatment),
                    intents = setOf(Intent.CONSOLIDATION)
                )
            )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(patientRecord))
    }

    @Test
    fun `Should be undetermined when undeterminted metastatic cancer`() {
        val alwaysUndeterminedMetastaticCancerEvaluation = mockk<HasMetastaticCancer> {
            every { evaluate(any()) } returns EvaluationFactory.undetermined("tumor stage unknown")
        }
        val function = IsEligibleForFirstLinePalliativeChemotherapy(alwaysUndeterminedMetastaticCancerEvaluation)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(patientRecord))
    }
}