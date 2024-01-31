package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatment
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.clinical.datamodel.treatment.Radiotherapy
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import org.junit.Test

private const val YEAR = 2020
private const val MONTH = 5

class HasRecentlyReceivedRadiotherapyTest {
    private val function = HasRecentlyReceivedRadiotherapy(YEAR, MONTH)
    private val radiotherapy = Radiotherapy(name = "")


    @Test
    fun `Should fail with no treatment history`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail with recent treatment with wrong category`() {
        val wrongCategory = treatmentHistoryEntry(treatment("", false, setOf(TreatmentCategory.TRANSPLANTATION)), YEAR, MONTH)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistoryEntry(wrongCategory)))
    }

    @Test
    fun `Should pass with right category but no date`() {
        val rightCategoryNoDate = radiotherapy()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistoryEntry(rightCategoryNoDate)))
    }

    @Test
    fun `Should fail with right category but old date`() {
        val rightCategoryOldDate = radiotherapy(YEAR - 1)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistoryEntry(rightCategoryOldDate)))
    }

    @Test
    fun `Should fail with right category but old month`() {
        val rightCategoryOldMonth = radiotherapy(YEAR, MONTH - 1)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistoryEntry(rightCategoryOldMonth)))
    }

    @Test
    fun `Should pass with right category and recent year`() {
        val rightCategoryRecentYear = radiotherapy(YEAR)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistoryEntry(rightCategoryRecentYear)))
    }

    @Test
    fun `Should pass with right category and recent year and month`() {
        val rightCategoryRecentYearAndMonth = radiotherapy(YEAR, MONTH)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistoryEntry(rightCategoryRecentYearAndMonth)))
    }

    private fun radiotherapy(startYear: Int? = null, startMonth: Int? = null): TreatmentHistoryEntry {
        return treatmentHistoryEntry(radiotherapy, startYear, startMonth)
    }

    private fun treatmentHistoryEntry(treatment: Treatment, startYear: Int? = null, startMonth: Int? = null): TreatmentHistoryEntry {
        return TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(treatment), startYear = startYear, startMonth = startMonth)
    }

    private fun withTreatmentHistoryEntry(treatment: TreatmentHistoryEntry): PatientRecord {
        return withTreatmentHistory(listOf(treatment))
    }
}