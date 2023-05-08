package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutablePriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.clinical.datamodel.TreatmentCategory
import org.apache.logging.log4j.util.Strings
import org.junit.Test

class HasRecentlyReceivedRadiotherapyTest {
    @Test
    fun canEvaluate() {
        val year = 2020
        val month = 5
        val function = HasRecentlyReceivedRadiotherapy(year, month)

        // No prior tumor treatments
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withPriorTumorTreatments(emptyList())))

        // Wrong category
        val wrongCategory: PriorTumorTreatment = builder().addCategories(TreatmentCategory.IMMUNOTHERAPY).build()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withPriorTumorTreatment(wrongCategory)))

        // Right category but no date
        val rightCategoryNoDate: PriorTumorTreatment = radiotherapy().build()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withPriorTumorTreatment(rightCategoryNoDate)))

        // Right category but old date
        val rightCategoryOldDate: PriorTumorTreatment = radiotherapy().startYear(year - 1).build()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withPriorTumorTreatment(rightCategoryOldDate)))

        // Right category but old month
        val rightCategoryOldMonth: PriorTumorTreatment = radiotherapy().startYear(year).startMonth(month - 1).build()
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withPriorTumorTreatment(rightCategoryOldMonth)))

        // Right category and recent year
        val rightCategoryRecentYear: PriorTumorTreatment = radiotherapy().startYear(year).build()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withPriorTumorTreatment(rightCategoryRecentYear)))

        // Right category and recent year and month
        val rightCategoryRecentYearAndMonth: PriorTumorTreatment = radiotherapy().startYear(year).startMonth(month).build()
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withPriorTumorTreatment(rightCategoryRecentYearAndMonth)))
    }

    companion object {
        private fun radiotherapy(): ImmutablePriorTumorTreatment.Builder {
            return builder().addCategories(TreatmentCategory.RADIOTHERAPY)
        }

        private fun builder(): ImmutablePriorTumorTreatment.Builder {
            return ImmutablePriorTumorTreatment.builder().name(Strings.EMPTY).isSystemic(true)
        }

        private fun withPriorTumorTreatment(treatment: PriorTumorTreatment): PatientRecord {
            return withPriorTumorTreatments(listOf(treatment))
        }

        private fun withPriorTumorTreatments(treatments: List<PriorTumorTreatment>): PatientRecord {
            return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(
                    ImmutableClinicalRecord.builder()
                        .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                        .priorTumorTreatments(treatments)
                        .build()
                )
                .build()
        }
    }
}