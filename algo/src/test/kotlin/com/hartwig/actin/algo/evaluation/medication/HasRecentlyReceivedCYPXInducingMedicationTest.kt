package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.CypInteraction
import com.hartwig.actin.clinical.datamodel.ImmutableCypInteraction
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Test

class HasRecentlyReceivedCYPXInducingMedicationTest {
    @Test
    fun shouldPassWhenPatientRecentlyReceivedCYPInducingMedication() {
        val medications = listOf(
            TestMedicationFactory.builder().stopDate(EVALUATION_DATE).addCypInteractions(
                ImmutableCypInteraction.builder().cyp("9A9").type(CypInteraction.Type.INDUCER).strength(CypInteraction.Strength.STRONG)
                    .build()
            ).build()
        )
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldFailWhenPatientReceivedCYPInducingMedicationBeforeMinStopDate() {
        val medications = listOf(
            TestMedicationFactory.builder().stopDate(EVALUATION_DATE.minusWeeks(3)).addCypInteractions(
                ImmutableCypInteraction.builder().cyp("9A9").type(CypInteraction.Type.INDUCER).strength(CypInteraction.Strength.STRONG)
                    .build()
            ).build()
        )
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldFailWhenPatientDoesNotRecentlyReceivedCYPInducingMedication() {
        val medications = listOf(
            TestMedicationFactory.builder().stopDate(EVALUATION_DATE).addCypInteractions(
                ImmutableCypInteraction.builder().cyp("9A9").type(CypInteraction.Type.SUBSTRATE).strength(CypInteraction.Strength.STRONG)
                    .build()
            ).build()
        )
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldFailWhenPatientUsesNoMedication() {
        val medications = listOf(TestMedicationFactory.builder().build())
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    companion object {
        private val EVALUATION_DATE = TestClinicalFactory.createMinimalTestClinicalRecord().patient().registrationDate().plusWeeks(1)
        private val FUNCTION =
            HasRecentlyReceivedCYPXInducingMedication(MedicationTestFactory.alwaysStopped(), "9A9", EVALUATION_DATE.minusDays(1))
    }
}