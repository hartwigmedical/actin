package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.washout.WashoutTestFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Test

private val MATCHING_CATEGORY = TreatmentCategory.TARGETED_THERAPY
private val MATCHING_TYPE_SET = setOf(DrugType.HER2_ANTIBODY)

class HasHadSomeTreatmentsWithCategoryOfTypesTest {

    private val function = HasHadSomeTreatmentsWithCategoryOfTypes(MATCHING_CATEGORY, MATCHING_TYPE_SET, 2)

    @Test
    fun `Should fail for no treatments`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail for wrong treatment category`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)))
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    @Test
    fun `Should fail for correct treatment category with wrong type`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", MATCHING_CATEGORY, setOf(DrugType.ANTI_TISSUE_FACTOR)))
        )
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    @Test
    fun `Should pass when treatments with correct category and type meet threshold`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY, MATCHING_TYPE_SET)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
        assertEvaluation(
            EvaluationResult.PASS, function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    @Test
    fun `Should pass for recent correct treatment category with incorrect type in treatment history entry but correct type in medication entry`() {
        val function = HasHadSomeTreatmentsWithCategoryOfTypes(MATCHING_CATEGORY, MATCHING_TYPE_SET, 1)
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", MATCHING_CATEGORY, setOf(DrugType.ANTI_TISSUE_FACTOR)))
        )
        val medication = WashoutTestFactory.medication().copy(
            drug = Drug(name = "", category = MATCHING_CATEGORY, drugTypes = MATCHING_TYPE_SET)
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TreatmentTestFactory.withTreatmentsAndMedications(listOf(treatmentHistoryEntry), listOf(medication)))
        )
    }

    @Test
    fun `Should return undetermined when treatments with correct category and no type meet threshold`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
        assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    @Test
    fun `Should return undetermined when trial treatments meet threshold`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY)), isTrial = true)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
        assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }

    @Test
    fun `Should ignore trial matches and fail when looking for unlikely trial categories`() {
        val function = HasHadSomeTreatmentsWithCategoryOfTypes(TreatmentCategory.TRANSPLANTATION, setOf(OtherTreatmentType.ALLOGENIC), 2)
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(treatment("test", true)), isTrial = true)
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }
}