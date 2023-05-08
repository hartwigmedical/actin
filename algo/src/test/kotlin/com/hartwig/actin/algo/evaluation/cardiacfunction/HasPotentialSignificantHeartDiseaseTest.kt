package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition
import com.hartwig.actin.doid.TestDoidModelFactory
import org.apache.logging.log4j.util.Strings
import org.junit.Test

class HasPotentialSignificantHeartDiseaseTest {
    @Test
    fun canEvaluate() {
        val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
        val function = HasPotentialSignificantHeartDisease(doidModel)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(CardiacFunctionTestFactory.withECG(null)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(CardiacFunctionTestFactory.withHasSignificantECGAberration(false)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(CardiacFunctionTestFactory.withHasSignificantECGAberration(true)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(CardiacFunctionTestFactory.withPriorOtherCondition(builder().build())))
        val firstDoid = HasPotentialSignificantHeartDisease.HEART_DISEASE_DOIDS.iterator().next()
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(CardiacFunctionTestFactory.withPriorOtherCondition(builder().addDoids(firstDoid).build()))
        )
        val firstTerm = HasPotentialSignificantHeartDisease.HEART_DISEASE_TERMS.iterator().next()
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                CardiacFunctionTestFactory.withPriorOtherCondition(
                    builder().name(
                        "this is a $firstTerm"
                    ).build()
                )
            )
        )
    }

    companion object {
        private fun builder(): ImmutablePriorOtherCondition.Builder {
            return ImmutablePriorOtherCondition.builder().name(Strings.EMPTY).category(Strings.EMPTY).isContraindicationForTherapy(true)
        }
    }
}