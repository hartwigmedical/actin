package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatment
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.withTreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.OtherTreatmentType
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import java.time.LocalDate
import org.junit.Test

class HasHadTreatmentWithCategoryButNotOfTypesRecentlyTest {
    @Test
    fun `Should fail for no treatments`() {
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail for trial treatment with unknown date`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(treatment("test", true)), isTrial = true)
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail for old trial treatment`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test",
                MATCHING_CATEGORY
            )), isTrial = true, startYear = MIN_DATE.year - 1
        )
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail for recent wrong treatment category`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)), startYear = MIN_DATE.year + 1
        )
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail for recent treatment with correct category and ignore type`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", MATCHING_CATEGORY, IGNORE_TYPE_SET)), startYear = MIN_DATE.year + 1
        )
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail for old treatment with correct category and matching type`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test",
                MATCHING_CATEGORY,
                IGNORE_TYPE_SET
            )), startYear = MIN_DATE.year - 1
        )
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should ignore trial matches and fail when looking for unlikely trial categories`() {
        val function =
            HasHadTreatmentWithCategoryButNotOfTypesRecently(TreatmentCategory.TRANSPLANTATION, setOf(OtherTreatmentType.ALLOGENIC),
                MIN_DATE
            )
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(treatment("test", true)), isTrial = true, startYear = MIN_DATE.year + 1
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should pass for recent treatment with correct category with other type`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test",
                MATCHING_CATEGORY,
                setOf(DrugType.ANTI_TISSUE_FACTOR)
            )), startYear = MIN_DATE.year + 1
        )
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should return undetermined for recent trial treatment`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(treatment("test", true)), isTrial = true, startYear = MIN_DATE.year + 1
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should return undetermined for treatment with correct category and with other type and unknown date`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test",
            MATCHING_CATEGORY,
            setOf(DrugType.ANTI_TISSUE_FACTOR)
        )))
        assertEvaluation(EvaluationResult.UNDETERMINED, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    companion object {
        private val MATCHING_CATEGORY = TreatmentCategory.TARGETED_THERAPY
        private val IGNORE_TYPE_SET = setOf(DrugType.HER2_ANTIBODY)
        private val MIN_DATE = LocalDate.of(2022, 4, 1)
        private val FUNCTION = HasHadTreatmentWithCategoryButNotOfTypesRecently(TreatmentCategory.TARGETED_THERAPY, IGNORE_TYPE_SET, MIN_DATE)
    }
}