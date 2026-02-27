package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.washout.WashoutTestFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentsAndMedications
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import java.time.LocalDate
import org.junit.jupiter.api.Test

private val MATCHING_CATEGORY = TreatmentCategory.TARGETED_THERAPY
private val DRUG_TYPE_TO_IGNORE_SET = setOf(DrugType.HER2_ANTIBODY)
private val OTHER_DRUG_TYPE_SET = setOf(DrugType.ANTI_TISSUE_FACTOR)
private val MIN_DATE = LocalDate.of(2022, 4, 1)

class HasHadTreatmentWithCategoryButNotOfTypesRecentlyTest {

    private val interpreter = WashoutTestFactory.activeFromDate(MIN_DATE)
    private val function =
        HasHadTreatmentWithCategoryButNotOfTypesRecently(TreatmentCategory.TARGETED_THERAPY, DRUG_TYPE_TO_IGNORE_SET, MIN_DATE, interpreter)

    @Test
    fun `Should fail for no treatments`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail for trial treatment with unknown date`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(treatment("test", true)), isTrial = true)
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
    fun `Should fail for recent wrong treatment category`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)), startYear = MIN_DATE.year + 1
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail for recent treatment with correct category and ignore type`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", MATCHING_CATEGORY, DRUG_TYPE_TO_IGNORE_SET)), startYear = MIN_DATE.year + 1
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail for old treatment with correct category and matching type`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", MATCHING_CATEGORY, DRUG_TYPE_TO_IGNORE_SET)), startYear = MIN_DATE.year - 1
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should ignore trial matches and fail when looking for unlikely trial categories`() {
        val function =
            HasHadTreatmentWithCategoryButNotOfTypesRecently(
                TreatmentCategory.TRANSPLANTATION, setOf(OtherTreatmentType.ALLOGENIC),
                MIN_DATE, interpreter
            )
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(treatment("test", true)), isTrial = true, startYear = MIN_DATE.year + 1
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should pass for recent treatment with correct category with other type`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(
                drugTreatment(
                    "test",
                    MATCHING_CATEGORY,
                    OTHER_DRUG_TYPE_SET
                )
            ), startYear = MIN_DATE.year + 1
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }


    @Test
    fun `Should fail for recent treatment with correct category and two types of which is one is ignored`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", MATCHING_CATEGORY, DRUG_TYPE_TO_IGNORE_SET + OTHER_DRUG_TYPE_SET)),
            startYear = MIN_DATE.year + 1
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should pass for recent treatment with correct category but ignored and other drug with correct treatment and other type`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(
                drugTreatment("test", MATCHING_CATEGORY, DRUG_TYPE_TO_IGNORE_SET),
                drugTreatment("test2", MATCHING_CATEGORY, OTHER_DRUG_TYPE_SET),
            ),
            startYear = MIN_DATE.year + 1
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should pass for recent treatment history entry with correct treatment category and incorrect type but medication entry with correct type`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", MATCHING_CATEGORY, DRUG_TYPE_TO_IGNORE_SET)), startYear = MIN_DATE.year + 1
        )
        val medication = WashoutTestFactory.medication(null, MIN_DATE.plusDays(1)).copy(
            drug = Drug(
                name = "", category = MATCHING_CATEGORY, drugTypes = setOf(DrugType.ANTI_TISSUE_FACTOR)
            )
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(withTreatmentsAndMedications(listOf(treatmentHistoryEntry), listOf(medication)))
        )
    }

    @Test
    fun `Should be undetermined for recent treatment with correct category of unknown type`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(
                drugTreatment(
                    "test",
                    MATCHING_CATEGORY,
                    emptySet()
                )
            ), startYear = MIN_DATE.year + 1
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should return undetermined for recent trial treatment`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(treatment("test", true)), isTrial = true, startYear = MIN_DATE.year + 1
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should return undetermined for treatment with correct category and with other type and unknown date`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", MATCHING_CATEGORY, OTHER_DRUG_TYPE_SET))
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }
}