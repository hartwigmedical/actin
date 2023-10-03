package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.medication.AtcTestFactory
import com.hartwig.actin.clinical.datamodel.ImmutableAtcLevel
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
        val atc =
            AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().code("wrong category").build())
                .build()
        val medications = listOf(WashoutTestFactory.builder().atc(atc).stopDate(REFERENCE_DATE.plusDays(1)).build())
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(WashoutTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldFailWhenMedicationHasRightCategoryAndOldDate() {
        val atc =
            AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().code("category to find").build())
                .build()
        val medications = listOf(WashoutTestFactory.builder().atc(atc).stopDate(REFERENCE_DATE.minusDays(1)).build())
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(WashoutTestFactory.withMedications(medications)))

    }

    @Test
    fun shouldPassWhenMedicationHasRightCategoryAndRecentDate() {
        val atc =
            AtcTestFactory.atcClassificationBuilder().anatomicalMainGroup(AtcTestFactory.atcLevelBuilder().code("category to find").build())
                .build()
        val medications = listOf(WashoutTestFactory.builder().atc(atc).stopDate(REFERENCE_DATE.plusDays(1)).build())
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(WashoutTestFactory.withMedications(medications)))
    }

    @Test
    fun shouldPassWhenMedicationIsTrialMedication() {
        val medications = listOf(WashoutTestFactory.builder().isTrialMedication(true).stopDate(REFERENCE_DATE.plusDays(1)).build())
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(WashoutTestFactory.withMedications(medications)))
    }

    companion object {
        private val REFERENCE_DATE = LocalDate.of(2020, 6, 6)
        private val INTERPRETER = WashoutTestFactory.activeFromDate(REFERENCE_DATE)
        private val FUNCTION =
            HasRecentlyReceivedCancerTherapyOfCategory(
                mapOf(
                    "category to find" to setOf(
                        ImmutableAtcLevel.builder().code("category to find").name("").build()
                    )
                ),
                mapOf("categories to ignore" to setOf(ImmutableAtcLevel.builder().code("category to ignore").name("").build())),
                INTERPRETER
            )
    }
}