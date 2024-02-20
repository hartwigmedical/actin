package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import org.junit.Test

class HasHadClinicalBenefitFollowingSomeTreatmentTest {

    private val TARGET_TREATMENT = TreatmentTestFactory.treatment("Chemotherapy", true, setOf(TreatmentCategory.CHEMOTHERAPY))
    private val WRONG_TREATMENT = TreatmentTestFactory.treatment("Radiotherapy", false, setOf(TreatmentCategory.RADIOTHERAPY))
    private val function = HasHadClinicalBenefitFollowingSomeTreatment(TARGET_TREATMENT)

    @Test
    fun `Should fail if treatment history is empty`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail if treatment history does not contain target treatment`() {
        val history = listOf(TreatmentTestFactory.treatmentHistoryEntry(setOf(WRONG_TREATMENT)))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withTreatmentHistory(history)))
    }

    @Test
    fun `Should pass if treatment history contains target therapy with best response complete response `() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TARGET_TREATMENT),
                bestResponse = TreatmentResponse.COMPLETE_RESPONSE
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withTreatmentHistory(history)))
    }

    @Test
    fun `Should pass if treatment history contains target therapy with best response partial response `() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TARGET_TREATMENT),
                bestResponse = TreatmentResponse.PARTIAL_RESPONSE
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withTreatmentHistory(history)))
    }

    @Test
    fun `Should pass if treatment history contains target therapy with best response remission `() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TARGET_TREATMENT),
                bestResponse = TreatmentResponse.REMISSION
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withTreatmentHistory(history)))
    }

    @Test
    fun `Should warn if treatment history contains target therapy with best response mixed response `() {

    }

    @Test
    fun `Should warn if treatment history contains target therapy with best response stable disease `() {

    }

    @Test
    fun `Should evaluate to undetermined if treatment history contains target therapy but no response specified`() {

    }

    @Test
    fun `Should fail if treatment history contains target therapy but best response progressive disease`() {

    }
}