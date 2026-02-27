package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.washout.WashoutTestFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Test

private val MATCHING_CATEGORY = TreatmentCategory.TARGETED_THERAPY
private val IGNORE_TYPE_SET = setOf(DrugType.HER2_ANTIBODY)

class HasHadTreatmentWithCategoryButNotOfTypesTest {

    private val function = HasHadTreatmentWithCategoryButNotOfTypes(MATCHING_CATEGORY, IGNORE_TYPE_SET)

    @Test
    fun `Should fail for no treatments`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail for wrong treatment category`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail for treatment with correct category and ignore type`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY, IGNORE_TYPE_SET)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should return undetermined for trial treatment`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(treatment("test", true)), isTrial = true)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should ignore trial matches and fail when looking for unlikely trial categories`() {
        val function = HasHadTreatmentWithCategoryButNotOfTypes(TreatmentCategory.TRANSPLANTATION, setOf(OtherTreatmentType.ALLOGENIC))
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(treatment("test", true)), isTrial = true)
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should pass for correct treatment category with other type`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment("test", MATCHING_CATEGORY, setOf(DrugType.ANTI_TISSUE_FACTOR)))
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should pass for correct treatment category with incorrect type in treatment history entry but correct type in medication entry`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY, IGNORE_TYPE_SET)))
        val medication = WashoutTestFactory.medication()
            .copy(drug = Drug(name = "", category = MATCHING_CATEGORY, drugTypes = setOf(DrugType.ANTI_TISSUE_FACTOR)))
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TreatmentTestFactory.withTreatmentsAndMedications(listOf(treatmentHistoryEntry), listOf(medication)))
        )
    }

    @Test
    fun `Should fail for treatment category with correct category but with type to ignore in treatment history entry and incorrect category but correct type in medication entry`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY, IGNORE_TYPE_SET)))
        val medication = WashoutTestFactory.medication()
            .copy(drug = Drug(name = "", category = TreatmentCategory.TRANSPLANTATION, drugTypes = setOf(DrugType.ANTI_TISSUE_FACTOR)))
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentsAndMedications(listOf(treatmentHistoryEntry), listOf(medication)))
        )
    }
}