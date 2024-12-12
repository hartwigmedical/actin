package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.TestIcdFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasSpecificInfectionTest {
    private val targetCodes = setOf(IcdConstants.ACUTE_HEPATITIS_B_CODE, IcdConstants.CHRONIC_HEPATITIS_B_CODE).map { IcdCode(it) }.toSet()
    private val function = HasSpecificInfection(TestIcdFactory.createTestModel(), targetCodes, "hepatitis B virus")

    @Test
    fun `Should fail with no prior conditions`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(InfectionTestFactory.withPriorOtherConditions(emptyList())))
    }

    @Test
    fun `Should fail with prior conditions but wrong ICD code`() {
        val condition = InfectionTestFactory.priorOtherCondition(icdCode = IcdCode(IcdConstants.CYTOMEGALOVIRAL_DISEASE_CODE))
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(InfectionTestFactory.withPriorOtherCondition(condition))
        )
    }

    @Test
    fun `Should pass for prior condition with correct ICD code`() {
        val condition = InfectionTestFactory.priorOtherCondition(icdCode = targetCodes.first())
        val evaluation = function.evaluate(InfectionTestFactory.withPriorOtherCondition(condition))
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.passGeneralMessages).containsExactly("Prior hepatitis B virus infection in history")
    }
}