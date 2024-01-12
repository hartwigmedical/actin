package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.AtcLevel
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Test

class HasRecentlyReceivedMedicationOfAtcLevelTest {
    @Test
    fun shouldFailWhenNoMedication() {
        val medications = emptyList<Medication>()
        assertEvaluation(EvaluationResult.FAIL, FUNCTION_ACTIVE.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldFailWhenMedicationHasWrongCategory() {
        val atc = AtcTestFactory.atcClassification("wrong category")
        val medications = listOf(TestMedicationFactory.createMinimal().copy(atc = atc))
        assertEvaluation(EvaluationResult.FAIL, FUNCTION_ACTIVE.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldPassWhenMedicationHasRightCategory() {
        val atc = AtcTestFactory.atcClassification("category to find")
        val medications = listOf(TestMedicationFactory.createMinimal().copy(atc = atc))
        assertEvaluation(EvaluationResult.PASS, FUNCTION_ACTIVE.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldPassWhenMedicationHasCorrectDate() {
        val function = HasRecentlyReceivedMedicationOfAtcLevel(
            MedicationTestFactory.alwaysStopped(),
            "category to find",
            setOf(AtcLevel(code = "category to find", name = "")),
            EVALUATION_DATE.minusDays(1)
        )
        val atc = AtcTestFactory.atcClassification("category to find")
        val medications = listOf(TestMedicationFactory.createMinimal().copy(atc = atc, stopDate = EVALUATION_DATE))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MedicationTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldBeUndeterminedWhenMedicationStoppedAfterMinStopDate() {
        val function = HasRecentlyReceivedMedicationOfAtcLevel(
            MedicationTestFactory.alwaysStopped(),
            "category to find",
            setOf(AtcLevel(code = "category to find", name = "")),
            EVALUATION_DATE.minusWeeks(2)
        )
        val atc = AtcTestFactory.atcClassification("category to find")
        val medications = listOf(TestMedicationFactory.createMinimal().copy(atc = atc, stopDate = EVALUATION_DATE))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(MedicationTestFactory.withMedications(medications))
        )
    }

    companion object {
        private val EVALUATION_DATE = TestClinicalFactory.createMinimalTestClinicalRecord().patient.registrationDate.plusWeeks(1)
        private val FUNCTION_ACTIVE = HasRecentlyReceivedMedicationOfAtcLevel(
            MedicationTestFactory.alwaysActive(),
            "category to find",
            setOf(AtcLevel(code = "category to find", name = "")),
            EVALUATION_DATE.plusDays(1)
        )
    }
}