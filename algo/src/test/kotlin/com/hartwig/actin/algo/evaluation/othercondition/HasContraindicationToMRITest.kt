package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.othercondition.ComorbidityTestFactory.intolerance
import com.hartwig.actin.algo.evaluation.othercondition.ComorbidityTestFactory.otherCondition
import com.hartwig.actin.algo.evaluation.othercondition.ComorbidityTestFactory.withOtherCondition
import com.hartwig.actin.algo.evaluation.othercondition.HasContraindicationToMRI.Companion.COMORBIDITIES_THAT_ARE_CONTRAINDICATIONS_TO_MRI
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.icd.TestIcdFactory
import org.junit.Test

class HasContraindicationToMRITest {
    private val function = HasContraindicationToMRI(TestIcdFactory.createTestModel())


    @Test
    fun `Should fail with no other condition`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComorbidityTestFactory.withOtherConditions(emptyList())))
    }

    @Test
    fun `Should fail with no relevant other condition`() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                ComorbidityTestFactory.withOtherConditions(
                    listOf(
                        otherCondition(icdMainCode = "wrong"),
                        otherCondition(name = "not a contraindication")
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass with a condition with correct ICD code`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(withOtherCondition(otherCondition(icdMainCode = IcdConstants.KIDNEY_FAILURE_BLOCK)))
        )
    }

    @Test
    fun `Should pass with a condition with correct name`() {
        COMORBIDITIES_THAT_ARE_CONTRAINDICATIONS_TO_MRI.forEach { contraindicationName ->
            assertEvaluation(EvaluationResult.PASS, function.evaluate(withOtherCondition(otherCondition(name = contraindicationName))))
        }
    }


    @Test
    fun `Should fail with no intolerances`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComorbidityTestFactory.withIntolerances(emptyList())))
    }

    @Test
    fun `Should fail with no relevant intolerance`() {
        val intolerances = listOf(intolerance("no relevant intolerance"))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComorbidityTestFactory.withIntolerances(intolerances)))
    }

    @Test
    fun `Should pass with relevant intolerance`() {
        COMORBIDITIES_THAT_ARE_CONTRAINDICATIONS_TO_MRI.forEach { contraindicationName ->
            val record = ComorbidityTestFactory.withIntolerances(listOf(intolerance(contraindicationName)))
            assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
        }
    }
}