package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.TestIcdFactory
import org.junit.Test

class HasHadPriorConditionWithIcdCodeFromSetTest {
    private val targetIcdCodes = IcdConstants.RESPIRATORY_COMPROMISE_SET.map { IcdCode(it) }.toSet()
    private val function = HasHadPriorConditionWithIcdCodeFromSet(TestIcdFactory.createTestModel(), targetIcdCodes, "respiratory compromise")

    @Test
    fun `Should pass if condition with correct ICD code in history`() {
        val conditions = OtherConditionTestFactory.priorOtherCondition("pneumonitis", icdMainCode = IcdConstants.PNEUMONITIS_BLOCK)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(conditions)))
    }

    @Test
    fun `Should fail if no conditions with correct ICD code in history`() {
        val conditions = OtherConditionTestFactory.priorOtherCondition("stroke", icdMainCode = IcdConstants.CEREBRAL_ISCHAEMIA_BLOCK)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(conditions)))
    }

    @Test
    fun `Should fail if no conditions present in history`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(emptyList())))
    }
}