package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.PriorOtherCondition
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasPotentialSignificantHeartDiseaseTest {
    @Test
    fun canEvaluate() {
        val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
        val function = HasPotentialSignificantHeartDisease(doidModel)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(CardiacFunctionTestFactory.withECG(null)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(CardiacFunctionTestFactory.withHasSignificantECGAberration(false)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(CardiacFunctionTestFactory.withHasSignificantECGAberration(true)))
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(CardiacFunctionTestFactory.withPriorOtherCondition(priorOtherCondition()))
        )
        val firstDoid = HasPotentialSignificantHeartDisease.HEART_DISEASE_DOIDS.iterator().next()
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(CardiacFunctionTestFactory.withPriorOtherCondition(priorOtherCondition(doid = firstDoid)))
        )
        val firstTerm = HasPotentialSignificantHeartDisease.HEART_DISEASE_TERMS.iterator().next()
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(CardiacFunctionTestFactory.withPriorOtherCondition(priorOtherCondition(name = "this is a $firstTerm")))
        )
    }

    companion object {
        private fun priorOtherCondition(name: String = "", doid: String? = null): PriorOtherCondition {
            return PriorOtherCondition(name = name, category = "", isContraindicationForTherapy = true, doids = setOfNotNull(doid))
        }
    }
}