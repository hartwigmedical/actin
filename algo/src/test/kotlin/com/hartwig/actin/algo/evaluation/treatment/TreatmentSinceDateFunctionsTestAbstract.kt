package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import org.junit.Test
import java.time.LocalDate

abstract class TreatmentSinceDateFunctionsTestAbstract {

    abstract fun functionForDate(minDate: LocalDate): EvaluationFunction

    abstract fun matchingTreatment(
        stopYear: Int?, stopMonth: Int?, startYear: Int? = null, startMonth: Int? = null
    ): TreatmentHistoryEntry

    @Test
    fun shouldFailWhenTreatmentNotFound() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function().evaluate(TreatmentTestFactory.withTreatmentHistory(listOf(NON_MATCHING_TREATMENT)))
        )
    }

    @Test
    fun shouldFailWhenMatchingTreatmentIsOlderByYear() {
        val treatmentHistory = listOf(NON_MATCHING_TREATMENT, olderTreatment())
        assertEvaluation(EvaluationResult.FAIL, function().evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun shouldFailWhenMatchingTreatmentIsOlderByMonth() {
        val olderDate = TARGET_DATE.minusMonths(1)
        val treatmentHistory = listOf(NON_MATCHING_TREATMENT, matchingTreatment(olderDate.year, olderDate.monthValue))
        assertEvaluation(EvaluationResult.FAIL, function().evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun shouldPassWhenTreatmentHistoryIncludesMatchingTreatmentWithinRange() {
        val treatmentHistory = listOf(NON_MATCHING_TREATMENT, olderTreatment(), matchingTreatment(RECENT_DATE.year, RECENT_DATE.monthValue))
        assertEvaluation(EvaluationResult.PASS, function().evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun shouldReturnUndeterminedWhenMatchingTreatmentHasUnknownYear() {
        val treatmentHistory = listOf(NON_MATCHING_TREATMENT, olderTreatment(), matchingTreatment(null, 10))
        assertEvaluation(EvaluationResult.UNDETERMINED, function().evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun shouldReturnUndeterminedWhenMatchingTreatmentMatchesYearWithUnknownMonth() {
        val treatmentHistory = listOf(NON_MATCHING_TREATMENT, matchingTreatment(TARGET_DATE.year, null))
        assertEvaluation(EvaluationResult.UNDETERMINED, function().evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun shouldFailWhenPriorTreatmentHasUnknownStopDateButOlderStartDate() {
        val olderDate = NOW.minusYears(YEARS_TO_SUBTRACT.toLong())
        val treatmentHistory = listOf(NON_MATCHING_TREATMENT, matchingTreatment(null, null, olderDate.year, olderDate.monthValue))
        assertEvaluation(EvaluationResult.FAIL, function().evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun shouldPassWhenPriorTreatmentHasUnknownStopDateButStartDateInRange() {
        val treatmentHistory = listOf(
            NON_MATCHING_TREATMENT,
            matchingTreatment(NOW.minusYears(YEARS_TO_SUBTRACT.toLong()).year, null),
            matchingTreatment(NOW.year, null, RECENT_DATE.year, RECENT_DATE.monthValue)
        )
        assertEvaluation(EvaluationResult.PASS, function().evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory)))
    }

    private fun function() = functionForDate(TARGET_DATE)

    private fun olderTreatment() = matchingTreatment(NOW.minusYears(YEARS_TO_SUBTRACT.toLong()).year, null)

    companion object {
        private val NOW = LocalDate.now()
        private val TARGET_DATE = NOW.minusYears(1)
        private val RECENT_DATE = NOW.minusMonths(4)
        private val NON_MATCHING_TREATMENT = treatmentHistoryEntry(
            setOf(treatment("other", false)), startYear = NOW.year, startMonth = NOW.monthValue
        )
        private const val YEARS_TO_SUBTRACT = 3
    }
}