package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.icd.TestIcdFactory
import org.junit.Test

class HasHadPriorConditionWithIcdTest {

    private val icdModel = TestIcdFactory.createModelWithSpecificNodes(listOf("conditionParent", "condition"))
    private val function = HasHadPriorConditionWithIcd(icdModel, "conditionParentCode")

    @Test
    fun `Should pass for prior condition with direct ICD code match`() {
        val conditions = listOf(OtherConditionTestFactory.priorOtherCondition(icdCode = "conditionParentCode"))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)))
    }

    @Test
    fun `Should pass for prior condition with matching parent icd code`() {
        val conditions = listOf(OtherConditionTestFactory.priorOtherCondition(icdCode = "conditionCode"))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)))
    }

    @Test
    fun `Should fail for prior condition with non-matching icd code`() {
        val conditions = listOf(OtherConditionTestFactory.priorOtherCondition(icdCode = "wrongCode"))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)))
    }

    @Test
    fun `Should fail for no prior conditions`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(emptyList())))
    }
}