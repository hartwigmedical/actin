package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory.intolerance
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory.priorOtherCondition
import com.hartwig.actin.algo.evaluation.othercondition.OtherConditionTestFactory.withPriorOtherCondition
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasContraindicationToMRITest {
    private val function = HasContraindicationToMRI(TestDoidModelFactory.createMinimalTestDoidModel())


    @Test
    fun `Should fail with no prior other condition`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(emptyList())))
    }

    @Test
    fun `Should fail with no relevant prior other condition`() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                OtherConditionTestFactory.withPriorOtherConditions(
                    listOf(
                        priorOtherCondition(doids = setOf("wrong doid")),
                        priorOtherCondition(name = "not a contraindication")
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass with a condition with correct DOID`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(withPriorOtherCondition(priorOtherCondition(doids = setOf(DoidConstants.KIDNEY_DISEASE_DOID))))
        )
    }

    @Test
    fun `Should pass with a condition with correct name`() {
        val contraindicationName = HasContraindicationToMRI.OTHER_CONDITIONS_BEING_CONTRAINDICATIONS_TO_MRI.first()
        assertEvaluation(
            EvaluationResult.PASS, function.evaluate(withPriorOtherCondition(priorOtherCondition(name = contraindicationName)))
        )
    }


    @Test
    fun `Should fail with no intolerances`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withIntolerances(emptyList())))
    }

    @Test
    fun `Should fail with no relevant intolerance`() {
        val intolerances = listOf(intolerance("no relevant intolerance"))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withIntolerances(intolerances)))
    }

    @Test
    fun `Should pass with relevant intolerance`() {
        val intolerances = listOf(intolerance(HasContraindicationToMRI.INTOLERANCES_BEING_CONTRAINDICATIONS_TO_MRI.first()))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withIntolerances(intolerances)))
    }
}