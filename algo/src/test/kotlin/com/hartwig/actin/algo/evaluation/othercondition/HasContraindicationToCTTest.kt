package com.hartwig.actin.algo.evaluation.othercondition

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.Complication
import com.hartwig.actin.clinical.datamodel.ImmutableComplication
import com.hartwig.actin.clinical.datamodel.ImmutableIntolerance
import com.hartwig.actin.clinical.datamodel.Intolerance
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import com.hartwig.actin.doid.TestDoidModelFactory
import org.apache.logging.log4j.util.Strings
import org.junit.Test

class HasContraindicationToCTTest {
    @Test
    fun canEvaluateOnPriorOtherCondition() {
        val function = createTestContraindicationCTFunction()

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
            HasContraindicationToCT.OTHER_CONDITIONS_BEING_CONTRAINDICATIONS_TO_CT.iterator().next()
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
        val function = createTestContraindicationCTFunction()

        // Test without intolerances
        val intolerances: MutableList<Intolerance> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withIntolerances(intolerances)))

        // Test no relevant intolerance
        intolerances.add(intoleranceBuilder().name("no relevant allergy").build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withIntolerances(intolerances)))

        // Test relevant intolerance
        val relevantAllergy: String = HasContraindicationToCT.INTOLERANCES_BEING_CONTRAINDICATIONS_TO_CT.iterator().next()
        intolerances.add(intoleranceBuilder().name(relevantAllergy).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withIntolerances(intolerances)))
    }

    @Test
    fun canEvaluateOnMedication() {
        val function = createTestContraindicationCTFunction()

        // Test without medications
        val medications: MutableList<Medication> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withMedications(medications)))

        // Test no relevant medication
        medications.add(TestMedicationFactory.builder().name("no relevant medication").build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withMedications(medications)))

        // Test relevant medication
        val relevantMedication: String = HasContraindicationToCT.MEDICATIONS_BEING_CONTRAINDICATIONS_TO_CT.iterator().next()
        medications.add(TestMedicationFactory.builder().name(relevantMedication).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withMedications(medications)))
    }

    @Test
    fun canEvaluateOnComplication() {
        val function = createTestContraindicationCTFunction()

        // Test without complications
        val medications: MutableList<Complication> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withComplications(medications)))

        // Test no relevant complication
        medications.add(ImmutableComplication.builder().name("no relevant complication").build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(OtherConditionTestFactory.withComplications(medications)))

        // Test relevant medication
        val relevantComplication: String = HasContraindicationToCT.COMPLICATIONS_BEING_CONTRAINDICATIONS_TO_CT.iterator().next()
        medications.add(ImmutableComplication.builder().name(relevantComplication).build())
        assertEvaluation(EvaluationResult.PASS, function.evaluate(OtherConditionTestFactory.withComplications(medications)))
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

        private fun createTestContraindicationCTFunction(): HasContraindicationToCT {
            return HasContraindicationToCT(TestDoidModelFactory.createMinimalTestDoidModel())
        }
    }
}