package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.CypInteraction
import com.hartwig.actin.clinical.datamodel.ImmutableCypInteraction
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Test

class CurrentlyGetsCypXInhibitingMedicationTest {

    private val alwaysActiveFunction = CurrentlyGetsCypXInhibitingMedication(MedicationTestFactory.alwaysActive(), "9A9")
    private val alwaysPlannedFunction = CurrentlyGetsCypXInhibitingMedication(MedicationTestFactory.alwaysPlanned(), "9A9")

    @Test
    fun `Should pass when CYP inhibiting medication`() {
        val medications = listOf(
            TestMedicationFactory.builder().addCypInteractions(
                ImmutableCypInteraction.builder().cyp("9A9").type(CypInteraction.Type.INHIBITOR).strength(CypInteraction.Strength.STRONG)
                    .build()
            ).build()
        )
        assertEvaluation(EvaluationResult.PASS, alwaysActiveFunction.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should fail when CYP inhibiting medication that does not match CYP`() {
        val medications = listOf(
            TestMedicationFactory.builder().addCypInteractions(
                ImmutableCypInteraction.builder().cyp("3A4").type(CypInteraction.Type.INHIBITOR).strength(CypInteraction.Strength.STRONG)
                    .build()
            ).build()
        )
        assertEvaluation(EvaluationResult.FAIL, alwaysActiveFunction.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should fail when no CYP inhibiting medication`() {
        val medications = listOf(
            TestMedicationFactory.builder().addCypInteractions(
                ImmutableCypInteraction.builder().cyp("9A9").type(CypInteraction.Type.SUBSTRATE).strength(CypInteraction.Strength.STRONG)
                    .build()
            ).build()
        )
        assertEvaluation(EvaluationResult.FAIL, alwaysActiveFunction.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should fail when patient uses no medication`() {
        val medications = listOf(TestMedicationFactory.builder().build())
        assertEvaluation(EvaluationResult.FAIL, alwaysActiveFunction.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should warn when patient plans to use CYP inhibiting medication`() {
        val medications = listOf(
            TestMedicationFactory.builder().addCypInteractions(
                ImmutableCypInteraction.builder().cyp("9A9").type(CypInteraction.Type.INHIBITOR).strength(CypInteraction.Strength.STRONG)
                    .build()
            ).build()
        )
        assertEvaluation(EvaluationResult.WARN, alwaysPlannedFunction.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should fail when patient plans to use medication which is not CYP inhibiting`() {
        val medications = listOf(
            TestMedicationFactory.builder().addCypInteractions(
                ImmutableCypInteraction.builder().cyp("9A9").type(CypInteraction.Type.SUBSTRATE).strength(CypInteraction.Strength.STRONG)
                    .build()
            ).build()
        )
        assertEvaluation(EvaluationResult.FAIL, alwaysPlannedFunction.evaluate(MedicationTestFactory.withMedications(medications)))
    }
}