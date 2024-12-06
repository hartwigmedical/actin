package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.icd.TestIcdFactory
import org.junit.Test

class HasPotentialAbsorptionDifficultiesTest {
    private val function = HasPotentialAbsorptionDifficulties(TestIcdFactory.createTestModel())
    private val correctIcd = IcdConstants.POSSIBLE_ABSORPTION_DIFFICULTIES_LIST.iterator().next()
    private val wrongIcd = "wrongIcd"
    private val correctCondition = OtherConditionTestFactory.priorOtherCondition(icdCode = correctIcd)
    private val correctComplication = OtherConditionTestFactory.complication(icdCode = correctIcd)
    private val correctToxicity = OtherConditionTestFactory.toxicity("", ToxicitySource.EHR, 2, correctIcd)

    @Test
    fun `Should pass for icd-matching prior other condition`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(correctCondition)))
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
            OtherConditionTestFactory.withToxicities(listOf(correctToxicity.copy(icdCode = wrongIcd))),
            OtherConditionTestFactory.withComplications(listOf(correctComplication.copy(icdCode = wrongIcd))),
            OtherConditionTestFactory.withPriorOtherCondition(correctCondition.copy(icdCode = wrongIcd))
        )
            .forEach {
                assertEvaluation(EvaluationResult.FAIL, function.evaluate((it)))
            }
    }

    @Test
    fun `Should fail for empty history`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                TestPatientFactory.createMinimalTestWGSPatientRecord()
                    .copy(priorOtherConditions = emptyList(), toxicities = emptyList(), complications = emptyList())
            )
        )
    }
}