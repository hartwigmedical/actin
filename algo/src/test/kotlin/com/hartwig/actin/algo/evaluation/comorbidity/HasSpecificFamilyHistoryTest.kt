package com.hartwig.actin.algo.evaluation.comorbidity

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.TestIcdFactory
import org.junit.jupiter.api.Test

class HasSpecificFamilyHistoryTest {

    private val undeterminedConditions =
        UndeterminedFamilyConditions("cardiovascular disease", setOf(IcdCode(IcdConstants.FAMILY_HISTORY_OF_CARDIOVASCULAR_DISEASE_CODE)))
    private val passConditions = PassFamilyConditions("sudden death", setOf(IcdCode("familySuddenDeathCode")))
    private val function =
        HasSpecificFamilyHistory(TestIcdFactory.createTestModel(), "idiopathic sudden death", passConditions, undeterminedConditions)

    @Test
    fun `Should fail when no prior conditions present`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(ComorbidityTestFactory.withOtherConditions((emptyList()))),
            "No presence of family history of idiopathic sudden death"
        )
    }

    @Test
    fun `Should fail when no conditions belonging to 'pass conditions', 'undetermined conditions', 'other specified', or unspecified family history present`() {
        val condition = ComorbidityTestFactory.otherCondition(icdMainCode = IcdConstants.HEART_FAILURE_BLOCK)
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(ComorbidityTestFactory.withOtherCondition(condition)),
            "No presence of family history of idiopathic sudden death"
        )
    }

    @Test
    fun `Should pass when 'pass condition' family history present`() {
        val condition = ComorbidityTestFactory.otherCondition(icdMainCode = passConditions.icdCodes.first().mainCode)
        val evaluation = function.evaluate(ComorbidityTestFactory.withOtherCondition(condition))
        assertEvaluation(EvaluationResult.PASS, evaluation, "Has family history of idiopathic sudden death")
    }

    @Test
    fun `Should evaluate to undetermined when 'undetermined condition' family history present`() {
        val condition =
            ComorbidityTestFactory.otherCondition(
                name = "acute myocard infarct",
                icdMainCode = undeterminedConditions.icdCodes.first().mainCode
            )
        val evaluation = function.evaluate(ComorbidityTestFactory.withOtherCondition(condition))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluation,
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
                    ComorbidityTestFactory.withOtherCondition(ComorbidityTestFactory.otherCondition(icdMainCode = it))
                ),
                "Has family history of unspecified disease () - undetermined if idiopathic sudden death"
            )
        }
    }
}