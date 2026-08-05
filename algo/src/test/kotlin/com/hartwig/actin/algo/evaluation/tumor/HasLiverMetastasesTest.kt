package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.configuration.ReportIntendedUse
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasLiverMetastasesTest {

    private val labels = EvaluationLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY).tumor
    private val function: HasLiverMetastases = HasLiverMetastases(labels)

    @Test
    fun shouldBeUndeterminedWhenHasLiverLesionsIsNull() {
        val undetermined = function.evaluate(TumorTestFactory.withLiverLesions(null))
        assertEvaluation(EvaluationResult.UNDETERMINED, undetermined)
    }

    @Test
    fun shouldPassWhenHasLiverLesionsIsTrue() {
        val pass = function.evaluate(TumorTestFactory.withLiverLesions(true))
        assertEvaluation(EvaluationResult.PASS, pass)
    }

    @Test
    fun shouldFailWhenHasLiverLesionsIsFalse() {
        val fail = function.evaluate(TumorTestFactory.withLiverLesions(false))
        assertEvaluation(EvaluationResult.FAIL, fail)
    }
}