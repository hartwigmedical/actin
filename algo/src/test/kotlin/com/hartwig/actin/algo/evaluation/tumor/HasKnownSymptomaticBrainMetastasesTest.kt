package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class HasKnownSymptomaticBrainMetastasesTest {

    private val function = HasKnownSymptomaticBrainMetastases()

    @Test
    fun `Should return undetermined when unknown if (symptomatic) brain metastases present`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withBrainLesionStatus(hasBrainLesions = null, hasSymptomaticBrainLesions = null))
        )
    }

    @Test
    fun `Should return undetermined when brain metastases present but unknown if symptomatic`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withBrainLesionStatus(hasBrainLesions = true, hasSymptomaticBrainLesions = null))
        )
    }

    @Test
    fun `Should fail when there are no brain metastases`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withBrainLesionStatus(hasBrainLesions = false, hasSymptomaticBrainLesions = null))
        )
    }

    @Test
    fun `Should fail when brain metastases are present but not symptomatic`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withBrainLesionStatus(hasBrainLesions = true, hasSymptomaticBrainLesions = false))
        )
    }

    @Test
    fun `Should pass when brain metastases are present and symptomatic`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withBrainLesionStatus(hasBrainLesions = true, hasSymptomaticBrainLesions = true))
        )
    }
}