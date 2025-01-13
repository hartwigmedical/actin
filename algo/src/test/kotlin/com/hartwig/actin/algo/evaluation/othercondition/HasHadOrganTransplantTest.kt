package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory.priorOtherCondition
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory.withPriorOtherConditions
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.icd.TestIcdFactory
import org.junit.Test

class HasHadOrganTransplantTest {
    private val function = HasHadOrganTransplant(TestIcdFactory.createTestModel(), null)
    private val functionWithMinYear = HasHadOrganTransplant(TestIcdFactory.createTestModel(), 2021)

    @Test
    fun `Should fail with no prior conditions`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withPriorOtherConditions(emptyList())))
    }

    @Test
    fun `Should fail with no relevant prior condition`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(withPriorOtherConditions(listOf(priorOtherCondition(icdMainCode = IcdConstants.PNEUMOTHORAX_CODE))))
        )
    }

    @Test
    fun `Should pass with relevant prior condition`() {
        assertEvaluation(
            EvaluationResult.PASS, function.evaluate(
                withPriorOtherConditions(
                    listOf(priorOtherCondition(icdMainCode = IcdConstants.TRANSPLANTATION_SET.first()))
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
                    listOf(priorOtherCondition(year = 2020, icdMainCode = IcdConstants.TRANSPLANTATION_SET.first()))
                )
            )
        )
    }

    @Test
    fun `Should be undetermined when transplant year is unclear`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED, functionWithMinYear.evaluate(
                withPriorOtherConditions(
                    listOf(priorOtherCondition(year = null, icdMainCode = IcdConstants.TRANSPLANTATION_SET.first()))
                )
            )
        )
    }

    @Test
    fun `Should pass when transplant occurred in min year`() {
        assertEvaluation(
            EvaluationResult.PASS, functionWithMinYear.evaluate(
                withPriorOtherConditions(
                    listOf(priorOtherCondition(year = 2021, icdMainCode = IcdConstants.TRANSPLANTATION_SET.first()))
                )
            )
        )
    }
}