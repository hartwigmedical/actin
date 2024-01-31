package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.medication.AtcTestFactory
import com.hartwig.actin.clinical.datamodel.AtcLevel
import com.hartwig.actin.clinical.datamodel.Medication
import org.junit.Test
import java.time.LocalDate

class HasRecentlyReceivedCancerTherapyOfCategoryTest {
    @Test
    fun shouldFailWhenNoMedication() {
        val medications = emptyList<Medication>()
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(WashoutTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldFailWhenMedicationHasWrongCategory() {
        val atc = AtcTestFactory.atcClassification("wrong category")
        val medications = listOf(WashoutTestFactory.medication(atc, REFERENCE_DATE.plusDays(1)))
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(WashoutTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldFailWhenMedicationHasRightCategoryAndOldDate() {
        val atc = AtcTestFactory.atcClassification("category to find")
        val medications = listOf(WashoutTestFactory.medication(atc, REFERENCE_DATE.minusDays(1)))
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(WashoutTestFactory.withMedications(medications)))

    }

    @Test
    fun shouldPassWhenMedicationHasRightCategoryAndRecentDate() {
        val atc = AtcTestFactory.atcClassification("category to find")
        val medications = listOf(WashoutTestFactory.medication(atc, REFERENCE_DATE.plusDays(1)))
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(WashoutTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldPassWhenMedicationIsTrialMedication() {
        val medications = listOf(WashoutTestFactory.medication(isTrialMedication = true, stopDate = REFERENCE_DATE.plusDays(1)))
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(WashoutTestFactory.withMedications(medications)))
    }

    companion object {
        private val REFERENCE_DATE = LocalDate.of(2020, 6, 6)
        private val INTERPRETER = WashoutTestFactory.activeFromDate(REFERENCE_DATE)
        private val FUNCTION = HasRecentlyReceivedCancerTherapyOfCategory(
            mapOf("category to find" to setOf(AtcLevel(code = "category to find", name = ""))),
            mapOf("categories to ignore" to setOf(AtcLevel(code = "category to ignore", name = ""))),
            INTERPRETER
        )
    }
}