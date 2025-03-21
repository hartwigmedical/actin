package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.TestIcdFactory
import org.junit.Test

class HasHadOtherConditionWithIcdCodeFromSetTest {
    private val targetIcdCodes = IcdConstants.RESPIRATORY_COMPROMISE_SET.map { IcdCode(it) }.toSet()
    private val function =
        HasHadOtherConditionWithIcdCodeFromSet(TestIcdFactory.createTestModel(), targetIcdCodes, "respiratory compromise")

    @Test
    fun `Should pass if condition with correct ICD code in history`() {
        val conditions = ComorbidityTestFactory.otherCondition("pneumonitis", icdMainCode = IcdConstants.PNEUMONITIS_DUE_TO_EXTERNAL_AGENTS_BLOCK)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ComorbidityTestFactory.withOtherCondition(conditions)))
    }

    @Test
    fun `Should evaluate to undetermined for condition with unknown extension`() {
        val function = HasHadOtherConditionWithIcdCodeFromSet(
            TestIcdFactory.createTestModel(),
            setOf(IcdCode(IcdConstants.PNEUMONITIS_DUE_TO_EXTERNAL_AGENTS_BLOCK, "extensionCode")),
            "respiratory compromise"
        )
        val conditions = ComorbidityTestFactory.otherCondition(
            "pneumonitis",
            icdMainCode = IcdConstants.PNEUMONITIS_DUE_TO_EXTERNAL_AGENTS_BLOCK,
            icdExtensionCode = null
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(ComorbidityTestFactory.withOtherCondition(conditions)))
    }

    @Test
    fun `Should fail if no conditions with correct ICD code in history`() {
        val conditions = ComorbidityTestFactory.otherCondition("stroke", icdMainCode = IcdConstants.CEREBRAL_ISCHAEMIA_BLOCK)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComorbidityTestFactory.withOtherCondition(conditions)))
    }

    @Test
    fun `Should fail if no conditions present in history`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComorbidityTestFactory.withOtherConditions(emptyList())))
    }
}