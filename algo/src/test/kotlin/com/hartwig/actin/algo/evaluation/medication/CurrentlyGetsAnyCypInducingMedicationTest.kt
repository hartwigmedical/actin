package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.CypInteraction
import com.hartwig.actin.clinical.datamodel.ImmutableCypInteraction
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Test

class CurrentlyGetsAnyCypInducingMedicationTest {

    private val alwaysActiveFunction = CurrentlyGetsAnyCypInducingMedication(MedicationTestFactory.alwaysActive())
    private val alwaysPlannedFunction = CurrentlyGetsAnyCypInducingMedication(MedicationTestFactory.alwaysPlanned())

    @Test
    fun `Should pass with any CYP inducing medication`() {
        val medications = listOf(
            TestMedicationFactory.builder().addCypInteractions(
                ImmutableCypInteraction.builder().cyp("9A9").type(CypInteraction.Type.INDUCER).strength(CypInteraction.Strength.STRONG)
                    .build()
            ).build()
        )
        assertEvaluation(EvaluationResult.PASS, alwaysActiveFunction.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should fail with no CYP inducing medication`() {
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
    fun `Should warn when patient plans to use CYP inducing medication`() {
        val medications = listOf(
            TestMedicationFactory.builder().addCypInteractions(
                ImmutableCypInteraction.builder().cyp("9A9").type(CypInteraction.Type.INDUCER).strength(CypInteraction.Strength.STRONG)
                    .build()
            ).build()
        )
        assertEvaluation(EvaluationResult.WARN, alwaysPlannedFunction.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should fail when patient plans to medication which is not CYP inducing`() {
        val medications = listOf(
            TestMedicationFactory.builder().addCypInteractions(
                ImmutableCypInteraction.builder().cyp("9A9").type(CypInteraction.Type.SUBSTRATE).strength(CypInteraction.Strength.STRONG)
                    .build()
            ).build()
        )
        assertEvaluation(EvaluationResult.FAIL, alwaysPlannedFunction.evaluate(MedicationTestFactory.withMedications(medications)))
    }
}