package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import org.junit.Test

class GetsHerbalMedicineMedicationTest {
    private val alwaysActiveFunction = GetsHerbalMedicineMedication(MedicationTestFactory.alwaysActive())
    private val alwaysPlannedFunction = GetsHerbalMedicineMedication(MedicationTestFactory.alwaysPlanned())
    private val alwaysInactiveFunction = GetsHerbalMedicineMedication(MedicationTestFactory.alwaysInactive())

    @Test
    fun `Should fail when patient uses no medications`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            alwaysActiveFunction.evaluate(MedicationTestFactory.withMedications(emptyList()))
        )
    }

    @Test
    fun `Should be fail when no self care medication`() {
        val medications = listOf(MedicationTestFactory.medication())
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            alwaysActiveFunction.evaluate(
                MedicationTestFactory.withMedications(medications)
            )
        )
    }

    @Test
    fun `Should be undetermined when medication is self care and active`() {
        val medications = listOf(MedicationTestFactory.medication(isSelfCare = true))
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            alwaysActiveFunction.evaluate(
                MedicationTestFactory.withMedications(medications)
            )
        )
    }

    @Test
    fun `Should be warn when medication is self care but planned`() {
        val medications = listOf(MedicationTestFactory.medication(isSelfCare = true))
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            alwaysPlannedFunction.evaluate(
                MedicationTestFactory.withMedications(medications)
            )
        )
    }

    @Test
    fun `Should be fail when medication is self care but inactive`() {
        val medications = listOf(MedicationTestFactory.medication(isSelfCare = true))
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            alwaysInactiveFunction.evaluate(
                MedicationTestFactory.withMedications(medications)
            )
        )
    }
}