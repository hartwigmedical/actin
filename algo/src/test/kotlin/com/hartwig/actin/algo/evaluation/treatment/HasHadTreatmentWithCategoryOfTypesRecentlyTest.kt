package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.washout.WashoutTestFactory
import com.hartwig.actin.datamodel.PatientRecord
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
    private val functionWithTypes =
        HasHadTreatmentWithCategoryOfTypesRecently(TreatmentCategory.TARGETED_THERAPY, MATCHING_TYPE_SET, MIN_DATE, interpreter)
    private val functionWithoutTypes =
        HasHadTreatmentWithCategoryOfTypesRecently(TreatmentCategory.TARGETED_THERAPY, null, MIN_DATE, interpreter)

    @Test
    fun `Should fail for no treatments`() {
        assertBothFunctions(EvaluationResult.FAIL, withTreatmentHistory(emptyList()))
    }

    @Test
    fun `Should fail for recent wrong treatment category`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)), startYear = MIN_DATE.year + 1
        )
        assertBothFunctions(EvaluationResult.FAIL, withTreatmentHistory(listOf(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail for recent correct treatment category with other type when type requested`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", MATCHING_CATEGORY, setOf(DrugType.ANTI_TISSUE_FACTOR))), startYear = MIN_DATE.year + 1
        )
        assertEvaluation(EvaluationResult.FAIL, functionWithTypes.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail for recent correct treatment category with unknown type when types requested`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", MATCHING_CATEGORY)), startYear = MIN_DATE.year + 1
        )
        assertEvaluation(EvaluationResult.FAIL, functionWithTypes.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should pass for recent correct treatment category with unknown type when types not requested`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", MATCHING_CATEGORY)), startYear = MIN_DATE.year + 1
        )
        assertEvaluation(EvaluationResult.PASS, functionWithoutTypes.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail for trial treatment with unknown date when types requested`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY)), isTrial = true)
        assertEvaluation(EvaluationResult.FAIL, functionWithTypes.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should be undetermined for trial treatment with unknown date when types not requested`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY)), isTrial = true)
        assertEvaluation(EvaluationResult.UNDETERMINED, functionWithoutTypes.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail for old trial treatment`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", MATCHING_CATEGORY)), isTrial = true, startYear = MIN_DATE.year - 1
        )
        assertBothFunctions(EvaluationResult.FAIL, withTreatmentHistoryEntry(treatmentHistoryEntry))
    }

    @Test
    fun `Should return undetermined for recent trial treatment when types requested`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", MATCHING_CATEGORY)), isTrial = true, startYear = MIN_DATE.year + 1
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, functionWithTypes.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should pass for recent trial treatment when types not requested`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", MATCHING_CATEGORY)), isTrial = true, startYear = MIN_DATE.year + 1
        )
        assertEvaluation(EvaluationResult.PASS, functionWithoutTypes.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
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
    fun `Should fail for old treatment with correct category and matching type (or not requiring type)`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", MATCHING_CATEGORY, MATCHING_TYPE_SET)), startYear = MIN_DATE.year - 1
        )
        assertBothFunctions(EvaluationResult.FAIL, withTreatmentHistoryEntry(treatmentHistoryEntry))
    }

    @Test
    fun `Should return undetermined for treatment with correct category and matching type (or not requiring type) and unknown date`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY, MATCHING_TYPE_SET)))
        assertBothFunctions(EvaluationResult.UNDETERMINED, withTreatmentHistoryEntry(treatmentHistoryEntry))
    }

    @Test
    fun `Should pass for recent treatment with correct category and matching type when requesting type`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", MATCHING_CATEGORY, MATCHING_TYPE_SET)), startYear = MIN_DATE.year + 1
        )
        assertEvaluation(EvaluationResult.PASS, functionWithTypes.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
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
        assertBothFunctions(
            EvaluationResult.PASS,
            withTreatmentsAndMedications(listOf(treatmentHistoryEntry), listOf(medication))
        )
    }

    private fun assertBothFunctions(result: EvaluationResult, record: PatientRecord) {
        assertEvaluation(result, functionWithTypes.evaluate(record))
        assertEvaluation(result, functionWithoutTypes.evaluate(record))
    }
}