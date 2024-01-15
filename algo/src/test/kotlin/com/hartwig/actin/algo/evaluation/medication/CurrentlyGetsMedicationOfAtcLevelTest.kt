package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.AtcLevel
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Test

class CurrentlyGetsMedicationOfAtcLevelTest {
    private val function =
        CurrentlyGetsMedicationOfAtcLevel(MedicationTestFactory.alwaysActive(), "L01A", setOf(AtcLevel(code = "L01A", name = "")))
    
    @Test
    fun `Should fail when no medication`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(emptyList())))
    }

    @Test
    fun `Should fail when medication has wrong category`() {
        val atc = AtcTestFactory.atcClassification("wrong category")
        val medications = listOf(TestMedicationFactory.createMinimal().copy(atc = atc))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun `Should pass when medication has right category`() {
        val atc = AtcTestFactory.atcClassification("L01A")
        assertEvaluation(
            EvaluationResult.PASS, function.evaluate(
                MedicationTestFactory.withMedications(listOf(TestMedicationFactory.createMinimal().copy(atc = atc)))
            )
        )
    }
}