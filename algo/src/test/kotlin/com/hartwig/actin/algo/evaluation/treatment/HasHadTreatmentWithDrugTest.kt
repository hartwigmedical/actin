package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.drugTreatmentNoDrugs
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatment
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.junit.Test

class HasHadTreatmentWithDrugTest {
   
    @Test
    fun `Should fail for empty treatment history`() {
        assertEvaluation(EvaluationResult.FAIL, FUNCTION_NO_SUBSTRING.evaluate(withTreatmentHistory(emptyList())))
        assertEvaluation(EvaluationResult.FAIL, FUNCTION_WITH_SUBSTRING.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail for non-drug treatment`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(setOf(treatment("other treatment", false))))
        assertEvaluation(EvaluationResult.FAIL, FUNCTION_NO_SUBSTRING.evaluate(withTreatmentHistory(treatmentHistory)))
        assertEvaluation(EvaluationResult.FAIL, FUNCTION_WITH_SUBSTRING.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should fail for therapy containing other drug`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(setOf(drugTreatment("other treatment", TREATMENT_CATEGORY))))
        assertEvaluation(EvaluationResult.FAIL, FUNCTION_NO_SUBSTRING.evaluate(withTreatmentHistory(treatmentHistory)))
        assertEvaluation(EvaluationResult.FAIL, FUNCTION_WITH_SUBSTRING.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should pass for therapy containing matching drug`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(setOf(drugTreatment(MATCHING_DRUG_NAME, TREATMENT_CATEGORY))))
        assertEvaluation(EvaluationResult.PASS, FUNCTION_NO_SUBSTRING.evaluate(withTreatmentHistory(treatmentHistory)))
        assertEvaluation(EvaluationResult.PASS, FUNCTION_WITH_SUBSTRING.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should pass for therapy containing matching substring of drug only if checking for substring is true`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(setOf(drugTreatment(MATCHING_SUBSTRING, TREATMENT_CATEGORY))))
        assertEvaluation(EvaluationResult.FAIL, FUNCTION_NO_SUBSTRING.evaluate(withTreatmentHistory(treatmentHistory)))
        assertEvaluation(EvaluationResult.PASS, FUNCTION_WITH_SUBSTRING.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should return undetermined for drug trial treatment with unknown drugs`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatmentNoDrugs("unknown drugs")), isTrial = true)
        assertEvaluation(EvaluationResult.UNDETERMINED, FUNCTION_NO_SUBSTRING.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            FUNCTION_WITH_SUBSTRING.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry)))
        )
    }

    @Test
    fun `Should fail for drug trial treatment with known drugs that don't match`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)), isTrial = true)
        assertEvaluation(EvaluationResult.FAIL, FUNCTION_NO_SUBSTRING.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
        assertEvaluation(EvaluationResult.FAIL, FUNCTION_WITH_SUBSTRING.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `Should fail for non-therapy trial treatments of known category`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(treatment("test", true, setOf(TreatmentCategory.SURGERY))), isTrial = true)
        assertEvaluation(EvaluationResult.FAIL, FUNCTION_NO_SUBSTRING.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
        assertEvaluation(EvaluationResult.FAIL, FUNCTION_WITH_SUBSTRING.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `Should return undetermined for non-therapy trial treatments of unknown category`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(treatment("unknown category", true)), isTrial = true)
        assertEvaluation(EvaluationResult.UNDETERMINED, FUNCTION_NO_SUBSTRING.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            FUNCTION_WITH_SUBSTRING.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry)))
        )
    }

    @Test
    fun `Should pass for drug trial treatment with matching drug`() {
        val treatmentHistoryEntry =
            treatmentHistoryEntry(setOf(drugTreatment("matching drug", TreatmentCategory.IMMUNOTHERAPY)), isTrial = true)
        assertEvaluation(EvaluationResult.PASS, FUNCTION_NO_SUBSTRING.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
        assertEvaluation(EvaluationResult.PASS, FUNCTION_WITH_SUBSTRING.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `Should be undetermined for trial with multiple drug therapy treatments including one of unknown drugs`() {
        val knownDrugTherapy = drugTreatment("other", TreatmentCategory.IMMUNOTHERAPY)
        val unknownDrugTherapy = drugTreatmentNoDrugs("unknown drugs")
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(knownDrugTherapy, unknownDrugTherapy), isTrial = true)
        assertEvaluation(EvaluationResult.UNDETERMINED, FUNCTION_NO_SUBSTRING.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            FUNCTION_WITH_SUBSTRING.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry)))
        )
    }

    companion object {
        private const val MATCHING_DRUG_NAME = "matching drug"
        private const val MATCHING_SUBSTRING = "match"
        private val TREATMENT_CATEGORY = TreatmentCategory.TARGETED_THERAPY
        private val FUNCTION_NO_SUBSTRING = HasHadTreatmentWithDrug(
            setOf(Drug(name = MATCHING_DRUG_NAME, category = TREATMENT_CATEGORY, drugTypes = emptySet())), checkSubString = false
        )
        private val FUNCTION_WITH_SUBSTRING = HasHadTreatmentWithDrug(
            setOf(Drug(name = MATCHING_SUBSTRING, category = TREATMENT_CATEGORY, drugTypes = emptySet())), checkSubString = true
        )
    }
}