package com.hartwig.actin.algo.evaluation.surgery

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.surgery.SurgeryTestFactory.builder
import com.hartwig.actin.algo.evaluation.surgery.SurgeryTestFactory.withSurgeries
import com.hartwig.actin.algo.evaluation.surgery.SurgeryTestFactory.withSurgery
import com.hartwig.actin.clinical.datamodel.Surgery
import com.hartwig.actin.clinical.datamodel.SurgeryStatus
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableOtherTreatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import org.junit.Test
import java.time.LocalDate


class HasHadAnySurgeryAfterSpecificDateTest {

    @Test
    fun shouldFailWithNoSurgeries() {
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withSurgeries(emptyList())))
    }

    @Test
    fun shouldFailWithOldSurgery() {
        val tooLongAgo: Surgery = builder().endDate(MIN_DATE.minusWeeks(4)).build()
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withSurgery(tooLongAgo)))
    }

    @Test
    fun shouldPassWithRecentSurgery() {
        val recentFinished: Surgery = builder().status(SurgeryStatus.FINISHED).endDate(MIN_DATE.plusWeeks(2)).build()
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(withSurgery(recentFinished)))
    }

    @Test
    fun shouldWarnWithRecentPlannedSurgery() {
        val recentPlanned: Surgery = builder().status(SurgeryStatus.PLANNED).endDate(MIN_DATE.plusWeeks(2)).build()
        assertEvaluation(EvaluationResult.WARN, FUNCTION.evaluate(withSurgery(recentPlanned)))
    }

    @Test
    fun shouldFailWithRecentCancelledSurgery() {
        val recentCancelled: Surgery = builder().status(SurgeryStatus.CANCELLED).endDate(MIN_DATE.plusWeeks(2)).build()
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withSurgery(recentCancelled)))
    }

    @Test
    fun shouldWarnWithFutureFinishedSurgery() {
        val futureFinished: Surgery = builder().status(SurgeryStatus.FINISHED).endDate(EVALUATION_DATE.plusWeeks(2)).build()
        assertEvaluation(EvaluationResult.WARN, FUNCTION.evaluate(withSurgery(futureFinished)))
    }

    @Test
    fun shouldPassWithFuturePlannedSurgery() {
        val futurePlanned: Surgery = builder().status(SurgeryStatus.PLANNED).endDate(EVALUATION_DATE.plusWeeks(2)).build()
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(withSurgery(futurePlanned)))
    }

    @Test
    fun shouldFailWithFutureCancelledSurgery() {
        val futureCancelled: Surgery = builder().status(SurgeryStatus.CANCELLED).endDate(EVALUATION_DATE.plusWeeks(2)).build()
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withSurgery(futureCancelled)))
    }

    @Test
    fun shouldFailWithNoPriorTreatments() {
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(SurgeryTestFactory.withOncologicalHistory(emptyList())))
    }

    @Test
    fun shouldFailWithRecentNonSurgicalTreatment() {
        val treatments = listOf(treatmentHistoryEntry(emptySet(), MIN_DATE.year))
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(SurgeryTestFactory.withOncologicalHistory(treatments)))
    }

    @Test
    fun shouldFailEvaluationWithTooLongAgoSurgicalTreatment() {
        val treatments = listOf(treatmentHistoryEntry(setOf(TreatmentCategory.SURGERY), MIN_DATE.minusYears(1).year))
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(SurgeryTestFactory.withOncologicalHistory(treatments)))
    }

    @Test
    fun shouldReturnUndeterminedWithSurgicalTreatmentInSameYear() {
        val treatments = listOf(treatmentHistoryEntry(setOf(TreatmentCategory.SURGERY), MIN_DATE.year))
        assertEvaluation(EvaluationResult.UNDETERMINED, FUNCTION.evaluate(SurgeryTestFactory.withOncologicalHistory(treatments)))
    }

    @Test
    fun shouldReturnUndeterminedWithSurgicalTreatmentWithoutDate() {
        val treatments = listOf(treatmentHistoryEntry(setOf(TreatmentCategory.SURGERY)))
        assertEvaluation(EvaluationResult.UNDETERMINED, FUNCTION.evaluate(SurgeryTestFactory.withOncologicalHistory(treatments)))
    }

    @Test
    fun shouldFailWithSurgicalTreatmentInMonthJustBeforeMinDate() {
        val treatments = listOf(treatmentHistoryEntry(setOf(TreatmentCategory.SURGERY), MIN_DATE.year, MIN_DATE.monthValue - 1))
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(SurgeryTestFactory.withOncologicalHistory(treatments)))
    }

    @Test
    fun shouldPassWithSurgicalTreatmentInMonthJustAfterMinDate() {
        val treatments = listOf(treatmentHistoryEntry(setOf(TreatmentCategory.SURGERY), MIN_DATE.year, MIN_DATE.monthValue + 1))
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(SurgeryTestFactory.withOncologicalHistory(treatments)))
    }

    companion object {
        private val EVALUATION_DATE = LocalDate.of(2020, 4, 20)
        private val MIN_DATE = EVALUATION_DATE.minusMonths(2)
        private val FUNCTION = HasHadAnySurgeryAfterSpecificDate(MIN_DATE, EVALUATION_DATE)

        private fun treatmentHistoryEntry(
            categories: Set<TreatmentCategory>,
            startYear: Int? = null,
            startMonth: Int? = null
        ): TreatmentHistoryEntry {
            return ImmutableTreatmentHistoryEntry.builder()
                .addTreatments(ImmutableOtherTreatment.builder().name("").isSystemic(false).categories(categories).build())
                .startYear(startYear)
                .startMonth(startMonth)
                .build()
        }
    }
}