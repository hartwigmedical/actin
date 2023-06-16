package com.hartwig.actin.algo.evaluation.surgery

import com.google.common.collect.Lists
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.treatment.ImmutablePriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.Surgery
import com.hartwig.actin.clinical.datamodel.SurgeryStatus
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.apache.logging.log4j.util.Strings
import org.junit.Test
import java.time.LocalDate

class HasHadAnySurgeryAfterSpecificDateTest {
    @Test
    fun canEvaluateBasedOnSurgeries() {
        val evaluationDate = LocalDate.of(2020, 4, 20)
        val minDate = evaluationDate.minusMonths(2)
        val function = HasHadAnySurgeryAfterSpecificDate(minDate, evaluationDate)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(SurgeryTestFactory.withSurgeries(Lists.newArrayList())))
        val tooLongAgo: Surgery = SurgeryTestFactory.builder().endDate(minDate.minusWeeks(4)).build()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(SurgeryTestFactory.withSurgery(tooLongAgo)))
        val recentFinished: Surgery = SurgeryTestFactory.builder().status(SurgeryStatus.FINISHED).endDate(minDate.plusWeeks(2)).build()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(SurgeryTestFactory.withSurgery(recentFinished)))
        val recentPlanned: Surgery = SurgeryTestFactory.builder().status(SurgeryStatus.PLANNED).endDate(minDate.plusWeeks(2)).build()
        assertEvaluation(EvaluationResult.WARN, function.evaluate(SurgeryTestFactory.withSurgery(recentPlanned)))
        val recentCancelled: Surgery = SurgeryTestFactory.builder().status(SurgeryStatus.CANCELLED).endDate(minDate.plusWeeks(2)).build()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(SurgeryTestFactory.withSurgery(recentCancelled)))
        val futureFinished: Surgery =
            SurgeryTestFactory.builder().status(SurgeryStatus.FINISHED).endDate(evaluationDate.plusWeeks(2)).build()
        assertEvaluation(EvaluationResult.WARN, function.evaluate(SurgeryTestFactory.withSurgery(futureFinished)))
        val futurePlanned: Surgery =
            SurgeryTestFactory.builder().status(SurgeryStatus.PLANNED).endDate(evaluationDate.plusWeeks(2)).build()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(SurgeryTestFactory.withSurgery(futurePlanned)))
        val futureCancelled: Surgery =
            SurgeryTestFactory.builder().status(SurgeryStatus.CANCELLED).endDate(evaluationDate.plusWeeks(2)).build()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(SurgeryTestFactory.withSurgery(futureCancelled)))
    }

    @Test
    fun canEvaluateBasedOnPriorTumorTreatments() {
        val evaluationDate = LocalDate.of(2020, 4, 20)
        val minDate = evaluationDate.minusMonths(2)
        val function = HasHadAnySurgeryAfterSpecificDate(minDate, evaluationDate)

        // No prior tumor treatments
        val treatments: MutableList<PriorTumorTreatment> = mutableListOf()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(SurgeryTestFactory.withPriorTumorTreatments(treatments)))

        // A non-surgery prior treatment
        treatments.add(builder().build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(SurgeryTestFactory.withPriorTumorTreatments(treatments)))

        // A surgery that is too long ago.
        treatments.add(builder().addCategories(TreatmentCategory.SURGERY).startYear(minDate.minusYears(1).year).build())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(SurgeryTestFactory.withPriorTumorTreatments(treatments)))

        // A surgery with just the same year (and no month).
        treatments.add(builder().addCategories(TreatmentCategory.SURGERY).startYear(minDate.year).build())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(SurgeryTestFactory.withPriorTumorTreatments(treatments)))

        // A surgery prior treatment with no date
        treatments.add(builder().addCategories(TreatmentCategory.SURGERY).build())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(SurgeryTestFactory.withPriorTumorTreatments(treatments)))

        // A surgery with a month just before min date.
        treatments.add(
            builder().addCategories(TreatmentCategory.SURGERY)
                .startYear(minDate.year)
                .startMonth(minDate.monthValue - 1)
                .build()
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(SurgeryTestFactory.withPriorTumorTreatments(treatments)))

        // A surgery with a month just after min date.
        treatments.add(
            builder().addCategories(TreatmentCategory.SURGERY)
                .startYear(minDate.year)
                .startMonth(minDate.monthValue + 1)
                .build()
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(SurgeryTestFactory.withPriorTumorTreatments(treatments)))
    }

    companion object {
        private fun builder(): ImmutablePriorTumorTreatment.Builder {
            return ImmutablePriorTumorTreatment.builder().name(Strings.EMPTY).isSystemic(true)
        }
    }
}