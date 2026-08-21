package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.jupiter.api.Test

class HasLungMetastasesTest {
    
    private val function: HasLungMetastases = HasLungMetastases()

    @Test
    fun shouldBeUndeterminedWhenHasLungLesionsIsNull() {
        val undetermined = function.evaluate(TumorTestFactory.withLungLesions(null))
        assertEvaluation(EvaluationResult.UNDETERMINED, undetermined, "Undetermined if patient has lung metastases (missing lesion data)")
    }

    @Test
    fun shouldPassWhenHasLungLesionsIsTrue() {
        val pass = function.evaluate(TumorTestFactory.withLungLesions(true))
        assertEvaluation(EvaluationResult.PASS, pass, "Has lung metastases")
    }

    @Test
    fun shouldFailWhenHasLungLesionsIsFalse() {
        val fail = function.evaluate(TumorTestFactory.withLungLesions(false))
        assertEvaluation(EvaluationResult.FAIL, fail, "No lung metastases")
    }
}