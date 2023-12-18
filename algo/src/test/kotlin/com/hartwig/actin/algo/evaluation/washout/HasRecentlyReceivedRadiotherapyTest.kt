package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableOtherTreatment
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableRadiotherapy
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import org.junit.Test

class HasRecentlyReceivedRadiotherapyTest {

    @Test
    fun shouldFailWithNoTreatmentHistory() {
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun shouldFailWithRecentTreatmentWithWrongCategory() {
        val wrongCategory = treatmentHistoryEntry(
            ImmutableOtherTreatment.builder().name("").isSystemic(false).addCategories(TreatmentCategory.TRANSPLANTATION).build(),
            YEAR,
            MONTH
        )
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistoryEntry(wrongCategory)))
    }

    @Test
    fun shouldPassWithRightCategoryButNoDate() {
        val rightCategoryNoDate = radiotherapy()
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistoryEntry(rightCategoryNoDate)))
    }

    @Test
    fun shouldFailWithRightCategoryButOldDate() {
        val rightCategoryOldDate = radiotherapy(YEAR - 1)
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistoryEntry(rightCategoryOldDate)))
    }

    @Test
    fun shouldFailWithRightCategoryButOldMonth() {
        val rightCategoryOldMonth = radiotherapy(YEAR, MONTH - 1)
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistoryEntry(rightCategoryOldMonth)))
    }

    @Test
    fun shouldPassWithRightCategoryAndRecentYear() {
        val rightCategoryRecentYear = radiotherapy(YEAR)
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistoryEntry(rightCategoryRecentYear)))
    }

    @Test
    fun shouldPassWithRightCategoryAndRecentYearAndMonth() {
        val rightCategoryRecentYearAndMonth = radiotherapy(YEAR, MONTH)
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistoryEntry(rightCategoryRecentYearAndMonth)))
    }

    companion object {
        private const val YEAR = 2020
        private const val MONTH = 5
        private val FUNCTION = HasRecentlyReceivedRadiotherapy(YEAR, MONTH)
        private val RADIOTHERAPY = ImmutableRadiotherapy.builder().name("").isSystemic(false).build()

        private fun radiotherapy(startYear: Int? = null, startMonth: Int? = null): TreatmentHistoryEntry {
            return treatmentHistoryEntry(RADIOTHERAPY, startYear, startMonth)
        }

        private fun treatmentHistoryEntry(treatment: Treatment, startYear: Int? = null, startMonth: Int? = null): TreatmentHistoryEntry {
            return ImmutableTreatmentHistoryEntry.builder()
                .addTreatments(treatment)
                .startYear(startYear)
                .startMonth(startMonth)
                .build()
        }

        private fun withTreatmentHistoryEntry(treatment: TreatmentHistoryEntry): PatientRecord {
            return withTreatmentHistory(listOf(treatment))
        }

        private fun withTreatmentHistory(treatmentHistory: List<TreatmentHistoryEntry>): PatientRecord {
            return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(
                    ImmutableClinicalRecord.builder()
                        .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                        .oncologicalHistory(treatmentHistory)
                        .build()
                )
                .build()
        }
    }
}