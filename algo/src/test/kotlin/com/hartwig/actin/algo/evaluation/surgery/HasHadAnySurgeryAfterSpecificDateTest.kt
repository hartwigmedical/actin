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
import java.time.LocalDate
import org.junit.jupiter.api.Test

class HasHadAnySurgeryAfterSpecificDateTest {
    
    private val evaluationDate = LocalDate.of(2020, 4, 20)
    private val minDate = evaluationDate.minusMonths(2)
    private val function = HasHadAnySurgeryAfterSpecificDate(minDate, evaluationDate)

    @Test
    fun `Should fail with no surgeries`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(withSurgeries(emptyList())),
            "No surgery after 20-Feb-2020 in provided treatments"
        )
    }

    @Test
    fun `Should fail with old surgery`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(withSurgery(surgery(minDate.minusWeeks(4)))),
            "No surgery after 20-Feb-2020 in provided treatments"
        )
    }

    @Test
    fun `Should return undetermined with surgery without end date`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(withSurgery(surgery(null))),
            "Date of surgery undetermined"
        )
    }

    @Test
    fun `Should pass with recent surgery`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(withSurgery(surgery(minDate.plusWeeks(2), SurgeryStatus.FINISHED))),
            "Surgery after 20-Feb-2020 in provided treatments"
        )
    }

    @Test
    fun `Should warn with recent planned surgery`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(withSurgery(surgery(minDate.plusWeeks(2), SurgeryStatus.PLANNED))),
            "Potential recent surgery"
        )
    }

    @Test
    fun `Should fail with recent cancelled surgery`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(withSurgery(surgery(minDate.plusWeeks(2), SurgeryStatus.CANCELLED))),
            "Recent surgery got cancelled"
        )
    }

    @Test
    fun `Should warn with future finished surgery`() {
        val futureFinished: Surgery = surgery(evaluationDate.plusWeeks(2), SurgeryStatus.FINISHED)
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(withSurgery(futureFinished)),
            "Potential recent surgery"
        )
    }

    @Test
    fun `Should warn with future planned surgery`() {
        val futurePlanned: Surgery = surgery(evaluationDate.plusWeeks(2), SurgeryStatus.PLANNED)
        assertEvaluation(EvaluationResult.WARN, function.evaluate(withSurgery(futurePlanned)), "Surgery is planned")
    }

    @Test
    fun `Should fail with future cancelled surgery`() {
        val futureCancelled: Surgery = surgery(evaluationDate.plusWeeks(2), SurgeryStatus.CANCELLED)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withSurgery(futureCancelled)), "Recent surgery got cancelled")
    }

    @Test
    fun `Should fail with no prior treatments`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(SurgeryTestFactory.withOncologicalHistory(emptyList())),
            "No surgery after 20-Feb-2020 in provided treatments"
        )
    }

    @Test
    fun `Should fail with recent non surgical treatment`() {
        val treatments = listOf(treatmentHistoryEntry(emptySet(), minDate.year))
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(SurgeryTestFactory.withOncologicalHistory(treatments)),
            "No surgery after 20-Feb-2020 in provided treatments"
        )
    }

    @Test
    fun `Should fail evaluation with too long ago surgical treatment`() {
        val treatments = listOf(treatmentHistoryEntry(setOf(TreatmentCategory.SURGERY), minDate.minusYears(1).year))
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(SurgeryTestFactory.withOncologicalHistory(treatments)),
            "No surgery after 20-Feb-2020 in provided treatments"
        )
    }

    @Test
    fun `Should return undetermined with surgical treatment in same year`() {
        val treatments = listOf(treatmentHistoryEntry(setOf(TreatmentCategory.SURGERY), minDate.year))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(SurgeryTestFactory.withOncologicalHistory(treatments)),
            "Undetermined if previous surgery is recent"
        )
    }

    @Test
    fun `Should return undetermined with surgical treatment without date`() {
        val treatments = listOf(treatmentHistoryEntry(setOf(TreatmentCategory.SURGERY)))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(SurgeryTestFactory.withOncologicalHistory(treatments)),
            "Undetermined if previous surgery is recent"
        )
    }

    @Test
    fun `Should fail with surgical treatment in month just before min date`() {
        val treatments = listOf(treatmentHistoryEntry(setOf(TreatmentCategory.SURGERY), minDate.year, minDate.monthValue - 1))
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(SurgeryTestFactory.withOncologicalHistory(treatments)),
            "No surgery after 20-Feb-2020 in provided treatments"
        )
    }

    @Test
    fun `Should pass with surgical treatment in month just after min date`() {
        val treatments = listOf(treatmentHistoryEntry(setOf(TreatmentCategory.SURGERY), minDate.year, minDate.monthValue + 1))
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(SurgeryTestFactory.withOncologicalHistory(treatments)),
            "Surgery after 20-Feb-2020 in provided treatments"
        )
    }

    @Test
    fun `Should pass with recent surgical treatment, even with other unexpected surgeries`() {
        val treatments = listOf(treatmentHistoryEntry(setOf(TreatmentCategory.SURGERY), minDate.year, minDate.monthValue + 1))
        val patient = withSurgery(surgery(evaluationDate.plusWeeks(2), SurgeryStatus.FINISHED))
            .copy(oncologicalHistory = treatments)
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(patient),
            "Surgery after 20-Feb-2020 in provided treatments"
        )
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