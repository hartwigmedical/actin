package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatment
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrug
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import org.junit.Test
import java.time.LocalDate

class HasHadSpecificTreatmentSinceDateTest {

    @Test
    fun shouldFailWhenTreatmentNotFound() {
        assertEvaluation(
            EvaluationResult.FAIL,
            FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(listOf(NON_MATCHING_TREATMENT)))
        )
    }

    @Test
    fun shouldFailWhenMatchingTreatmentIsOlderByYear() {
        val treatmentHistory = listOf(NON_MATCHING_TREATMENT, OLDER_TREATMENT)
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun shouldFailWhenMatchingTreatmentIsOlderByMonth() {
        val olderDate: LocalDate = TARGET_DATE.minusMonths(1)
        val treatmentHistory = listOf(NON_MATCHING_TREATMENT, matchingTreatment(olderDate.year, olderDate.monthValue))
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun shouldPassWhenTreatmentHistoryIncludesMatchingTreatmentWithinRange() {
        val treatmentHistory = listOf(NON_MATCHING_TREATMENT, OLDER_TREATMENT, matchingTreatment(RECENT_DATE.year, RECENT_DATE.monthValue))
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun shouldReturnUndeterminedWhenMatchingTreatmentHasUnknownYear() {
        val treatmentHistory = listOf(NON_MATCHING_TREATMENT, OLDER_TREATMENT, matchingTreatment(null, 10))
        assertEvaluation(EvaluationResult.UNDETERMINED, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun shouldReturnUndeterminedWhenMatchingTreatmentMatchesYearWithUnknownMonth() {
        val treatmentHistory = listOf(NON_MATCHING_TREATMENT, matchingTreatment(TARGET_DATE.year, null))
        assertEvaluation(EvaluationResult.UNDETERMINED, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun shouldFailWhenPriorTreatmentHasUnknownStopDateButOlderStartDate() {
        val olderDate: LocalDate = LocalDate.now().minusYears(YEARS_TO_SUBTRACT.toLong())
        val treatmentHistory = listOf(NON_MATCHING_TREATMENT, matchingTreatment(null, null, olderDate.year, olderDate.monthValue))
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun shouldPassWhenPriorTreatmentHasUnknownStopDateButStartDateInRange() {
        val treatmentHistory = listOf(
            NON_MATCHING_TREATMENT,
            matchingTreatment(LocalDate.now().minusYears(YEARS_TO_SUBTRACT.toLong()).year, null),
            matchingTreatment(LocalDate.now().year, null, RECENT_DATE.year, RECENT_DATE.monthValue)
        )
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)))
    }

    companion object {
        private val TREATMENT_QUERY = ImmutableDrugTreatment.builder()
            .name("treatment")
            .addDrugs(ImmutableDrug.builder().name("Chemo drug").category(TreatmentCategory.CHEMOTHERAPY).build())
            .build()

        private val TARGET_DATE: LocalDate = LocalDate.now().minusYears(1)
        private val RECENT_DATE: LocalDate = LocalDate.now().minusMonths(4)
        private val FUNCTION = HasHadSpecificTreatmentSinceDate(TREATMENT_QUERY, TARGET_DATE)
        private val NON_MATCHING_TREATMENT = treatmentHistoryEntry(
            setOf(treatment("other", false)),
            startYear = LocalDate.now().year, startMonth = LocalDate.now().monthValue
        )
        private const val YEARS_TO_SUBTRACT = 3
        private val OLDER_TREATMENT = matchingTreatment(LocalDate.now().minusYears(YEARS_TO_SUBTRACT.toLong()).year, null)

        private fun matchingTreatment(
            stopYear: Int?,
            stopMonth: Int?,
            startYear: Int? = null,
            startMonth: Int? = null
        ): TreatmentHistoryEntry {
            return treatmentHistoryEntry(setOf(TREATMENT_QUERY), startYear, startMonth, stopYear = stopYear, stopMonth = stopMonth)
        }
    }
}