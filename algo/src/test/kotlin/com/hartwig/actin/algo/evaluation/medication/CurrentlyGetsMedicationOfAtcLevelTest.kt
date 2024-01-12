package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.AtcLevel
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Test

class CurrentlyGetsMedicationOfAtcLevelTest {
    @Test
    fun shouldFailWhenNoMedication() {
        val medications = emptyList<Medication>()
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldFailWhenMedicationHasWrongCategory() {
        val atc = AtcTestFactory.atcClassification("wrong category")
        val medications = listOf(TestMedicationFactory.createMinimal().copy(atc = atc))
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldPassWhenMedicationHasRightCategory() {
        val atc = AtcTestFactory.atcClassification("L01A")
        assertEvaluation(
            EvaluationResult.PASS, FUNCTION.evaluate(
                MedicationTestFactory.withMedications(
                    listOf(TestMedicationFactory.createMinimal().copy(atc = atc))
                )
            )
        )
    }

    companion object {
        private val FUNCTION =
            CurrentlyGetsMedicationOfAtcLevel(
                MedicationTestFactory.alwaysActive(),
                "L01A",
                setOf(AtcLevel(code = "L01A", name = ""))
            )
    }
}