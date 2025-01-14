package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.icd.TestIcdFactory
import org.junit.Test

class HasPotentialAbsorptionDifficultiesTest {
    private val function = HasPotentialAbsorptionDifficulties(TestIcdFactory.createTestModel())
    private val correctIcd = IcdConstants.POSSIBLE_ABSORPTION_DIFFICULTIES_SET.iterator().next()
    private val wrongIcdMainCode = "wrong"
    private val correctCondition = OtherConditionTestFactory.otherCondition(icdMainCode = correctIcd)
    private val correctComplication = OtherConditionTestFactory.complication(icdMainCode = correctIcd)
    private val correctToxicity = OtherConditionTestFactory.toxicity("", ToxicitySource.EHR, 2, correctIcd)

    @Test
    fun `Should pass for icd-matching other condition`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withOtherCondition(correctCondition)))
    }

    @Test
    fun `Should pass for icd-matching complication`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withComplications(listOf(correctComplication))))
    }

    @Test
    fun `Should pass for icd-matching toxicity`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withToxicities(listOf(correctToxicity))))
    }

    @Test
    fun `Should fail when no matching condition, complication or toxicity present`() {
        listOf(
            OtherConditionTestFactory.withToxicities(listOf(correctToxicity.copy(icdCodes = setOf(IcdCode(wrongIcdMainCode))))),
            OtherConditionTestFactory.withComplications(listOf(correctComplication.copy(icdCodes = setOf(IcdCode(wrongIcdMainCode))))),
            OtherConditionTestFactory.withOtherCondition(correctCondition.copy(icdCodes = setOf(IcdCode(wrongIcdMainCode))))
        )
            .forEach {
                assertEvaluation(EvaluationResult.FAIL, function.evaluate((it)))
            }
    }

    @Test
    fun `Should fail for empty history`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }
}