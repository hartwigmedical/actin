package com.hartwig.actin.algo.evaluation.comorbidity

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.icd.TestIcdFactory
import org.junit.jupiter.api.Test

class HasPotentialDisruptionOfLymphaticDrainageTest {
    private val function = HasPotentialDisruptionOfLymphaticDrainage(TestIcdFactory.createTestModel())
    private val correctIcd = IcdConstants.LYMPHATIC_DRAINAGE_SET.iterator().next()
    private val wrongIcdMainCode = "wrong"
    private val correctCondition = ComorbidityTestFactory.otherCondition(icdMainCode = correctIcd)
    private val correctToxicity = ComorbidityTestFactory.toxicity("", ToxicitySource.EHR, 2, correctIcd)

    @Test
    fun `Should warn for icd-matching other condition`() {
        assertEvaluation(EvaluationResult.WARN, function.evaluate(ComorbidityTestFactory.withOtherCondition(correctCondition)))
    }

    @Test
    fun `Should warn for icd-matching toxicity`() {
        assertEvaluation(EvaluationResult.WARN, function.evaluate(ComorbidityTestFactory.withToxicities(listOf(correctToxicity))))
    }

    @Test
    fun `Should fail when no matching condition or toxicity present`() {
        listOf(
            ComorbidityTestFactory.withToxicities(listOf(correctToxicity.copy(icdCodes = setOf(IcdCode(wrongIcdMainCode))))),
            ComorbidityTestFactory.withOtherCondition(correctCondition.copy(icdCodes = setOf(IcdCode(wrongIcdMainCode))))
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
