package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableIntolerance
import com.hartwig.actin.clinical.datamodel.Intolerance
import com.hartwig.actin.doid.TestDoidModelFactory
import org.apache.logging.log4j.util.Strings
import org.junit.Test

class HasContraindicationToMRITest {
    @Test
    fun canEvaluateOnPriorOtherCondition() {
        val function = createTestContraindicationMRIFunction()

        // Test no prior other conditions
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(OtherConditionTestFactory.withPriorOtherConditions(emptyList()))
        )

        // Add a condition with wrong doid
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                OtherConditionTestFactory.withPriorOtherCondition(
                    OtherConditionTestFactory.builder()
                        .addDoids("wrong doid")
                        .build()
                )
            )
        )

        // Add a condition with correct DOID
        val contraindicationDoid = DoidConstants.KIDNEY_DISEASE_DOID
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                OtherConditionTestFactory.withPriorOtherCondition(
                    OtherConditionTestFactory.builder()
                        .addDoids(contraindicationDoid)
                        .build()
                )
            )
        )

        // Test with a condition with wrong name
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                OtherConditionTestFactory.withPriorOtherCondition(
                    OtherConditionTestFactory.builder()
                        .name("not a contraindication")
                        .build()
                )
            )
        )

        // Test with a condition with correct name
        val contraindicationName: String =
            HasContraindicationToMRI.OTHER_CONDITIONS_BEING_CONTRAINDICATIONS_TO_MRI.iterator().next()
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                OtherConditionTestFactory.withPriorOtherCondition(
                    OtherConditionTestFactory.builder()
                        .name(contraindicationName)
                        .build()
                )
            )
        )
    }

    @Test
    fun canEvaluateOnIntolerance() {
        val function = createTestContraindicationMRIFunction()

        // Test without intolerances
        val intolerances: MutableList<Intolerance> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withIntolerances(intolerances)))

        // Test no relevant intolerance
        intolerances.add(intoleranceBuilder().name("no relevant intolerance").build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withIntolerances(intolerances)))

        // Test relevant intolerance
        val relevantIntolerance: String = HasContraindicationToMRI.INTOLERANCES_BEING_CONTRAINDICATIONS_TO_MRI.iterator().next()
        intolerances.add(intoleranceBuilder().name(relevantIntolerance).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withIntolerances(intolerances)))
    }

    companion object {
        private fun intoleranceBuilder(): ImmutableIntolerance.Builder {
            return ImmutableIntolerance.builder()
                .name(Strings.EMPTY)
                .category(Strings.EMPTY)
                .type(Strings.EMPTY)
                .clinicalStatus(Strings.EMPTY)
                .verificationStatus(Strings.EMPTY)
                .criticality(Strings.EMPTY)
        }

        private fun createTestContraindicationMRIFunction(): HasContraindicationToMRI {
            return HasContraindicationToMRI(TestDoidModelFactory.createMinimalTestDoidModel())
        }
    }
}