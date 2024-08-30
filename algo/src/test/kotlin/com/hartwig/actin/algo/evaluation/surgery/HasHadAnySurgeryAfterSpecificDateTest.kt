package com.hartwig.actin.algo.evaluation.surgery

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.surgery.SurgeryTestFactory.surgery
import com.hartwig.actin.algo.evaluation.surgery.SurgeryTestFactory.withSurgeries
import com.hartwig.actin.algo.evaluation.surgery.SurgeryTestFactory.withSurgery
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.Surgery
import com.hartwig.actin.datamodel.clinical.SurgeryStatus
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import org.junit.Test
import java.time.LocalDate

class HasHadAnySurgeryAfterSpecificDateTest {
    private val evaluationDate = LocalDate.of(2020, 4, 20)
    private val minDate = evaluationDate.minusMonths(2)
    private val function = HasHadAnySurgeryAfterSpecificDate(minDate, evaluationDate)

    @Test
    fun `Should fail with no surgeries`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withSurgeries(emptyList())))
    }

    @Test
    fun `Should fail with old surgery`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withSurgery(surgery(minDate.minusWeeks(4)))))
    }

    @Test
    fun `Should pass with recent surgery`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withSurgery(surgery(minDate.plusWeeks(2), SurgeryStatus.FINISHED))))
    }

    @Test
    fun `Should warn with recent planned surgery`() {
        assertEvaluation(EvaluationResult.WARN, function.evaluate(withSurgery(surgery(minDate.plusWeeks(2), SurgeryStatus.PLANNED))))
    }

    @Test
    fun `Should fail with recent cancelled surgery`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withSurgery(surgery(minDate.plusWeeks(2), SurgeryStatus.CANCELLED))))
    }

    @Test
    fun `Should warn with future finished surgery`() {
        val futureFinished: Surgery = surgery(evaluationDate.plusWeeks(2), SurgeryStatus.FINISHED)
        assertEvaluation(EvaluationResult.WARN, function.evaluate(withSurgery(futureFinished)))
    }

    @Test
    fun `Should pass with future planned surgery`() {
        val futurePlanned: Surgery = surgery(evaluationDate.plusWeeks(2), SurgeryStatus.PLANNED)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withSurgery(futurePlanned)))
    }

    @Test
    fun `Should fail with future cancelled surgery`() {
        val futureCancelled: Surgery = surgery(evaluationDate.plusWeeks(2), SurgeryStatus.CANCELLED)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withSurgery(futureCancelled)))
    }

    @Test
    fun `Should fail with no prior treatments`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(SurgeryTestFactory.withOncologicalHistory(emptyList())))
    }

    @Test
    fun `Should fail with recent non surgical treatment`() {
        val treatments = listOf(treatmentHistoryEntry(emptySet(), minDate.year))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(SurgeryTestFactory.withOncologicalHistory(treatments)))
    }

    @Test
    fun `Should fail evaluation with too long ago surgical treatment`() {
        val treatments = listOf(treatmentHistoryEntry(setOf(TreatmentCategory.SURGERY), minDate.minusYears(1).year))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(SurgeryTestFactory.withOncologicalHistory(treatments)))
    }

    @Test
    fun `Should return undetermined with surgical treatment in same year`() {
        val treatments = listOf(treatmentHistoryEntry(setOf(TreatmentCategory.SURGERY), minDate.year))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(SurgeryTestFactory.withOncologicalHistory(treatments)))
    }

    @Test
    fun `Should return undetermined with surgical treatment without date`() {
        val treatments = listOf(treatmentHistoryEntry(setOf(TreatmentCategory.SURGERY)))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(SurgeryTestFactory.withOncologicalHistory(treatments)))
    }

    @Test
    fun `Should fail with surgical treatment in month just before min date`() {
        val treatments = listOf(treatmentHistoryEntry(setOf(TreatmentCategory.SURGERY), minDate.year, minDate.monthValue - 1))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(SurgeryTestFactory.withOncologicalHistory(treatments)))
    }

    @Test
    fun `Should pass with surgical treatment in month just after min date`() {
        val treatments = listOf(treatmentHistoryEntry(setOf(TreatmentCategory.SURGERY), minDate.year, minDate.monthValue + 1))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(SurgeryTestFactory.withOncologicalHistory(treatments)))
    }

    private fun treatmentHistoryEntry(
        categories: Set<TreatmentCategory>, startYear: Int? = null, startMonth: Int? = null
    ): TreatmentHistoryEntry {
        return TreatmentTestFactory.treatmentHistoryEntry(
            treatments = setOf(OtherTreatment(name = "", isSystemic = false, categories = categories)),
            startYear = startYear,
            startMonth = startMonth
        )
    }
}