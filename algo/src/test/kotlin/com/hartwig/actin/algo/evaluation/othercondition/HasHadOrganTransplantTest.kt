package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import org.junit.Test

class HasHadOrganTransplantTest {
    @Test
    fun canEvaluate() {
        val function = HasHadOrganTransplant(null)
        val conditions: MutableList<PriorOtherCondition> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)))
        conditions.add(OtherConditionTestFactory.builder().build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)))
        conditions.add(OtherConditionTestFactory.builder().category(HasHadOrganTransplant.ORGAN_TRANSPLANT_CATEGORY).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)))
    }

    @Test
    fun canEvaluateWithMinYear() {
        val function = HasHadOrganTransplant(2021)
        val conditions: MutableList<PriorOtherCondition> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)))
        val builder = OtherConditionTestFactory.builder().category(HasHadOrganTransplant.ORGAN_TRANSPLANT_CATEGORY)

        // Too long ago.
        conditions.add(builder.year(2020).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)))

        // Unclear date
        conditions.add(builder.year(null).build())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)))

        // Exact match
        conditions.add(builder.year(2021).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(conditions)))
    }
}