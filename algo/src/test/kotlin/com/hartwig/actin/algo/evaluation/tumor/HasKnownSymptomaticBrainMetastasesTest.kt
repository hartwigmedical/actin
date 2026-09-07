package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasKnownSymptomaticBrainMetastasesTest {

    private val function = HasKnownSymptomaticBrainMetastases()

    @Test
    fun `Should return undetermined when unknown if (symptomatic) brain metastases present`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withBrainLesionStatus(hasBrainLesions = null, hasSymptomaticBrainLesions = null)),
            "Undetermined if symptomatic brain metastases present (data missing)"
        )
    }

    @Test
    fun `Should return undetermined when brain metastases present but unknown if symptomatic`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withBrainLesionStatus(hasBrainLesions = true, hasSymptomaticBrainLesions = null)),
            "Brain metastases present but unknown if symptomatic (data missing)"
        )
    }

    @Test
    fun `Should fail when there are no brain metastases`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withBrainLesionStatus(hasBrainLesions = false, hasSymptomaticBrainLesions = null)),
            "No known symptomatic brain metastases present"
        )
    }

    @Test
    fun `Should fail when brain metastases are present but not symptomatic`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withBrainLesionStatus(hasBrainLesions = true, hasSymptomaticBrainLesions = false)),
            "No known symptomatic brain metastases present"
        )
    }

    @Test
    fun `Should pass when brain metastases are present and symptomatic`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withBrainLesionStatus(hasBrainLesions = true, hasSymptomaticBrainLesions = true)),
            "Has symptomatic brain metastases"
        )
    }
}