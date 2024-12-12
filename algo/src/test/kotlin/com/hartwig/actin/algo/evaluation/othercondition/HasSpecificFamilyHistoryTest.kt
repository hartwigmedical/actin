package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.TestIcdFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasSpecificFamilyHistoryTest {

    private val undeterminedConditions =
        UndeterminedFamilyConditions("cardiovascular disease", setOf(IcdCode(IcdConstants.FAMILY_HISTORY_OF_CARDIOVASCULAR_DISEASE_CODE)))
    private val passConditions = PassFamilyConditions("sudden death", setOf(IcdCode("familySuddenDeathCode")))
    private val function =
        HasSpecificFamilyHistory(TestIcdFactory.createTestModel(), "idiopathic sudden death", passConditions, undeterminedConditions)

    @Test
    fun `Should fail when no prior conditions present`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions((emptyList()))))
    }

    @Test
    fun `Should fail when no conditions belonging to 'pass conditions', 'undetermined conditions', 'other specified', or unspecified family history present`() {
        val condition = OtherConditionTestFactory.priorOtherCondition(icdMainCode = IcdConstants.HEART_FAILURE_BLOCK)
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(condition))
        )
    }

    @Test
    fun `Should pass when 'pass condition' family history present`() {
        val condition = OtherConditionTestFactory.priorOtherCondition(icdMainCode = passConditions.icdCodes.first().mainCode)
        val evaluation = function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(condition))
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.passGeneralMessages).containsExactly("Has family history of idiopathic sudden death")
    }

    @Test
    fun `Should evaluate to undetermined when 'undetermined condition' family history present`() {
        val condition =
            OtherConditionTestFactory.priorOtherCondition(name = "acute myocard infarct", icdMainCode = undeterminedConditions.icdCodes.first().mainCode)
        val evaluation = function.evaluate(OtherConditionTestFactory.withPriorOtherCondition(condition))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedGeneralMessages).containsExactly(
            "Has family history of cardiovascular disease (acute myocard infarct) - undetermined if idiopathic sudden death"
        )
    }

    @Test
    fun `Should evaluate to undetermined when unspecified or 'other specified' family history present`() {
        listOf(
            IcdConstants.FAMILY_HISTORY_OF_UNSPECIFIED_HEALTH_PROBLEMS_CODE,
            IcdConstants.FAMILY_HISTORY_OF_OTHER_SPECIFIED_HEALTH_PROBLEMS_CODE,
        ).forEach {
            assertEvaluation(
                EvaluationResult.UNDETERMINED,
                function.evaluate(
                    OtherConditionTestFactory.withPriorOtherCondition(OtherConditionTestFactory.priorOtherCondition(icdMainCode = it))
                )
            )
        }
    }
}