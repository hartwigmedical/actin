package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.TreatmentCategory
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import org.junit.Test
import java.time.LocalDate

class HasHadSpecificTreatmentSinceDateTest {

    private val function = HasHadSpecificTreatmentSinceDate(TREATMENT_QUERY, targetDate)

    @Test
    fun shouldFailWhenTreatmentNotFound() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(listOf(nonMatchingTreatment)))
        )
    }

    @Test
    fun shouldFailWhenMatchingTreatmentIsOlderByYear() {
        val priorTumorTreatments: List<PriorTumorTreatment> = listOf(nonMatchingTreatment, olderTreatment)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)))
    }

    @Test
    fun shouldFailWhenMatchingTreatmentIsOlderByMonth() {
        val olderDate: LocalDate = targetDate.minusMonths(1)
        val priorTumorTreatments: List<PriorTumorTreatment> =
            listOf(nonMatchingTreatment, matchingTreatment(olderDate.year, olderDate.monthValue))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)))
    }

    @Test
    fun shouldPassWhenPriorTreatmentsIncludeMatchingTreatmentWithinRange() {
        val priorTumorTreatments: List<PriorTumorTreatment> =
            listOf(nonMatchingTreatment, olderTreatment, matchingTreatment(recentDate.year, recentDate.monthValue))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)))
    }

    @Test
    fun shouldReturnUndeterminedWhenMatchingTreatmentHasUnknownYear() {
        val priorTumorTreatments: List<PriorTumorTreatment> = listOf(nonMatchingTreatment, matchingTreatment(null, 10))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments))
        )
    }

    @Test
    fun shouldReturnUndeterminedWhenMatchingTreatmentMatchesYearWithUnknownMonth() {
        val priorTumorTreatments: List<PriorTumorTreatment> = listOf(nonMatchingTreatment, matchingTreatment(targetDate.year, null))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments))
        )
    }

    @Test
    fun shouldFailWhenPriorTreatmentHasUnknownStopDateButOlderStartDate() {
        val olderDate: LocalDate = LocalDate.now().minusYears(YEARS_TO_SUBTRACT.toLong())
        val priorTumorTreatments: List<PriorTumorTreatment> = listOf(
            nonMatchingTreatment,
            olderTreatment,
            matchingTreatment(null, null, olderDate.year, olderDate.monthValue)
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)))
    }

    @Test
    fun shouldPassWhenPriorTreatmentHasUnknownStopDateButStartDateInRange() {
        val priorTumorTreatments: List<PriorTumorTreatment> = listOf(
            nonMatchingTreatment,
            matchingTreatment(LocalDate.now().minusYears(YEARS_TO_SUBTRACT.toLong()).year, null),
            matchingTreatment(LocalDate.now().year, null, recentDate.year, recentDate.monthValue)
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(priorTumorTreatments)))
    }

    companion object {
        private const val TREATMENT_QUERY = "treatment"
        private const val YEARS_TO_SUBTRACT = 3
        private val targetDate: LocalDate = LocalDate.now().minusYears(1)
        private val recentDate: LocalDate = LocalDate.now().minusMonths(4)
        private val nonMatchingTreatment: PriorTumorTreatment = TreatmentTestFactory.builder()
            .name("other")
            .addCategories(TreatmentCategory.RADIOTHERAPY)
            .startYear(LocalDate.now().year)
            .startMonth(LocalDate.now().monthValue)
            .build()
        private val olderTreatment: PriorTumorTreatment =
            matchingTreatment(LocalDate.now().minusYears(YEARS_TO_SUBTRACT.toLong()).year, null)

        private fun matchingTreatment(
            stopYear: Int?,
            stopMonth: Int?,
            startYear: Int? = null,
            startMonth: Int? = null
        ): PriorTumorTreatment {
            return TreatmentTestFactory.builder()
                .name("specific $TREATMENT_QUERY")
                .addCategories(TreatmentCategory.CHEMOTHERAPY)
                .stopYear(stopYear)
                .stopMonth(stopMonth)
                .startYear(startYear)
                .startMonth(startMonth)
                .build()
        }
    }
}