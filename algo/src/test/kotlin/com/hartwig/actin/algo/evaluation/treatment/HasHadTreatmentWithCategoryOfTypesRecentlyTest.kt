package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.washout.WashoutTestFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentsAndMedications
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Test
import java.time.LocalDate

private val MATCHING_CATEGORY = TreatmentCategory.TARGETED_THERAPY
private val MATCHING_TYPE_SET = setOf(DrugType.HER2_ANTIBODY)
private val MIN_DATE = LocalDate.of(2022, 4, 1)

class HasHadTreatmentWithCategoryOfTypesRecentlyTest {

    private val interpreter = WashoutTestFactory.activeFromDate(MIN_DATE)
    private val function =
        HasHadTreatmentWithCategoryOfTypesRecently(TreatmentCategory.TARGETED_THERAPY, MATCHING_TYPE_SET, MIN_DATE, interpreter)

    @Test
    fun `Should fail for no treatments`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail for recent wrong treatment category`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)), startYear = MIN_DATE.year + 1
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail for recent correct treatment category with other type`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", MATCHING_CATEGORY, setOf(DrugType.ANTI_TISSUE_FACTOR))), startYear = MIN_DATE.year + 1
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail for recent correct treatment category with unknown type`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", MATCHING_CATEGORY)), startYear = MIN_DATE.year + 1
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail for trial treatment with unknown date`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY)), isTrial = true)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail for old trial treatment`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", MATCHING_CATEGORY)), isTrial = true, startYear = MIN_DATE.year - 1
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should return undetermined for recent trial treatment`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", MATCHING_CATEGORY)), isTrial = true, startYear = MIN_DATE.year + 1
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should ignore trial matches and fail when looking for unlikely trial categories`() {
        val function =
            HasHadTreatmentWithCategoryOfTypesRecently(
                TreatmentCategory.TRANSPLANTATION,
                setOf(OtherTreatmentType.ALLOGENIC),
                MIN_DATE,
                interpreter
            )
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", TreatmentCategory.TRANSPLANTATION)), isTrial = true, startYear = MIN_DATE.year + 1
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail for old treatment with correct category and matching type`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", MATCHING_CATEGORY, MATCHING_TYPE_SET)), startYear = MIN_DATE.year - 1
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should return undetermined for treatment with correct category and matching type and unknown date`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY, MATCHING_TYPE_SET)))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should pass for recent treatment with correct category and matching type`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", MATCHING_CATEGORY, MATCHING_TYPE_SET)), startYear = MIN_DATE.year + 1
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should pass for recent correct treatment category with other type and medication with correct type`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", MATCHING_CATEGORY, setOf(DrugType.ANTI_TISSUE_FACTOR))), startYear = MIN_DATE.year + 1
        )
        val medication = WashoutTestFactory.medication(null, MIN_DATE.plusMonths(2)).copy(
            drug = Drug(
                name = "", category = MATCHING_CATEGORY, drugTypes =
                MATCHING_TYPE_SET
            ),
            startDate = MIN_DATE.plusMonths(1)
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(withTreatmentsAndMedications(listOf(treatmentHistoryEntry), listOf(medication)))
        )
    }
}