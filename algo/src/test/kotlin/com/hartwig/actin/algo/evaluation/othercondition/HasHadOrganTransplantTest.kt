package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory.priorOtherCondition
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory.withPriorOtherConditions
import com.hartwig.actin.datamodel.algo.EvaluationResult
import org.junit.Test

class HasHadOrganTransplantTest {
    private val function = HasHadOrganTransplant(null)
    private val functionWithMinYear = HasHadOrganTransplant(2021)

    @Test
    fun `Should fail with no prior conditions`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withPriorOtherConditions(emptyList())))
    }
    
    @Test
    fun `Should fail with no relevant prior condition`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withPriorOtherConditions(listOf(priorOtherCondition()))))
    }

    @Test
    fun `Should pass with relevant prior condition`() {
        assertEvaluation(
            EvaluationResult.PASS, function.evaluate(
                withPriorOtherConditions(
                    listOf(priorOtherCondition(category = HasHadOrganTransplant.ORGAN_TRANSPLANT_CATEGORY))
                )
            )
        )
    }

    @Test
    fun `Should fail with min year when there are no prior conditions`() {
        assertEvaluation(EvaluationResult.FAIL, functionWithMinYear.evaluate(withPriorOtherConditions(emptyList())))
    }

    @Test
    fun `Should fail when transplant occurred before min year`() {
        assertEvaluation(
            EvaluationResult.FAIL, functionWithMinYear.evaluate(
                withPriorOtherConditions(
                    listOf(priorOtherCondition(category = HasHadOrganTransplant.ORGAN_TRANSPLANT_CATEGORY, year = 2020))
                )
            )
        )
    }

    @Test
    fun `Should be undetermined when transplant year is unclear`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED, functionWithMinYear.evaluate(
                withPriorOtherConditions(
                    listOf(priorOtherCondition(category = HasHadOrganTransplant.ORGAN_TRANSPLANT_CATEGORY, year = null))
                )
            )
        )
    }

    @Test
    fun `Should pass when transplant occurred in min year`() {
        assertEvaluation(
            EvaluationResult.PASS, functionWithMinYear.evaluate(
                withPriorOtherConditions(
                    listOf(priorOtherCondition(category = HasHadOrganTransplant.ORGAN_TRANSPLANT_CATEGORY, year = 2021))
                )
            )
        )
    }
}