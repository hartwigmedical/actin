package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test
import org.junit.Assert.assertTrue

class HasMeasurableDiseaseRecistTest {
    @Test
    fun shouldPassWhenHasMeasurableDiseaseIsTrue() {
        val evaluation = FUNCTION.evaluate(TumorTestFactory.withMeasurableDisease(true))
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertTrue(evaluation.recoverable())
    }

    @Test
    fun shouldPassWhenHasMeasurableDiseaseIsTrueAndRandomDoid() {
        val evaluation = FUNCTION.evaluate(TumorTestFactory.withMeasurableDiseaseAndDoid(true, "random"))
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertTrue(evaluation.recoverable())
    }

    @Test
    fun shouldFailWhenHasMeasurableDiseaseIsFalse() {
        val evaluation = FUNCTION.evaluate(TumorTestFactory.withMeasurableDisease(false))
        assertEvaluation(EvaluationResult.FAIL, evaluation)
        assertTrue(evaluation.recoverable())
    }

    @Test
    fun shouldBeUndeterminedWhenHasMeasurableDiseaseIsUndetermined() {
        val evaluation = FUNCTION.evaluate(TumorTestFactory.withMeasurableDisease(null))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertTrue(evaluation.recoverable())
    }

    @Test
    fun shouldWarnWhenUncertainIfEvaluatedAgainstRecist() {
        val evaluation = FUNCTION.evaluate(
            TumorTestFactory.withMeasurableDiseaseAndDoid(
                true,
                HasMeasurableDiseaseRecist.NON_RECIST_TUMOR_DOIDS.iterator().next()
            )
        )
        assertEvaluation(EvaluationResult.WARN, evaluation)
    }

    companion object {
        private val DOID_MODEL = TestDoidModelFactory.createWithOneParentChild("100", "200")
        private val FUNCTION = HasMeasurableDiseaseRecist(DOID_MODEL)
    }
}