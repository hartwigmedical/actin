package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import org.junit.Test

class HasHadPDFollowingSomeSystemicTreatmentsTest {

    @Test
    fun `Should fail when treatment history is empty`() {
        FUNCTIONS.forEach {
            assertEvaluation(EvaluationResult.FAIL, it.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
        }
    }

    @Test
    fun `Should fail when history contains non-systemic treatments only`() {
        val treatments = listOf("1", "2").map { treatmentEntry(it, false, StopReason.PROGRESSIVE_DISEASE) }

        FUNCTIONS.forEach {
            assertEvaluation(EvaluationResult.FAIL, it.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
        }
    }

    @Test
    fun `Should fail when history contains less systemic treatments than requested`() {
        FUNCTIONS.forEach {
            assertEvaluation(
                EvaluationResult.FAIL,
                it.evaluate(TreatmentTestFactory.withTreatmentHistory(listOf(treatmentEntry("1", true))))
            )
        }
    }

    @Test
    fun `Should evaluate to undetermined when requested number of treatments is met only when counting duplicate treatments entries twice`() {
        val treatment = treatmentEntry("1", true, StopReason.PROGRESSIVE_DISEASE)

        FUNCTIONS.forEach {
            assertEvaluation(
                EvaluationResult.UNDETERMINED,
                it.evaluate(TreatmentTestFactory.withTreatmentHistory(listOf(treatment, treatment)))
            )
        }
    }

    @Test
    fun `Should evaluate to undetermined when history contains requested number of treatment lines but unknown PD status for some`() {
        val treatments = listOf("1" to StopReason.PROGRESSIVE_DISEASE, "2" to null).map {
            treatmentEntry(name = it.first, true, stopReason = it.second, stopYear = null)
        }

        FUNCTIONS.forEach {
            assertEvaluation(EvaluationResult.UNDETERMINED, it.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
        }
    }

    @Test
    fun `Should pass when history contains requested number of treatment lines with PD`() {
        val treatments = listOf("1", "2").map { treatmentEntry(it, true, StopReason.PROGRESSIVE_DISEASE) }
        FUNCTIONS.forEach {
            assertEvaluation(EvaluationResult.PASS, it.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
        }
    }

    @Test
    fun `Should pass when history contains requested number of treatment lines with PD but last treatment line is non-PD`() {
        val treatments = listOf("1", "2").map { treatmentEntry(it, true, StopReason.PROGRESSIVE_DISEASE, 2023) } +
                treatmentEntry("3", true, StopReason.TOXICITY, 2024)
        FUNCTIONS.forEach {
            assertEvaluation(EvaluationResult.PASS, it.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments)))
        }
    }

    private fun treatmentEntry(name: String, systemic: Boolean, stopReason: StopReason? = null, stopYear: Int? = null) =
        TreatmentTestFactory.treatmentHistoryEntry(
            treatments = listOf(TreatmentTestFactory.treatment(name, systemic)),
            stopReason = stopReason,
            stopYear = stopYear
        )

    companion object {
        private val FUNCTION = HasHadPDFollowingSomeSystemicTreatments(2, false)
        private val RADIOLOGICAL_FUNCTION = HasHadPDFollowingSomeSystemicTreatments(2, true)
        private val FUNCTIONS = listOf(FUNCTION, RADIOLOGICAL_FUNCTION)
    }
}