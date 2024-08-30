package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatmentNoDrugs
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Test

class HasHadTreatmentWithDrugTest {
   
    @Test
    fun `should fail for empty treatment history`() {
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `should fail for non-drug treatment`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(setOf(treatment("other treatment", false))))
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `should fail for therapy containing other drug`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(setOf(drugTreatment("other treatment", TREATMENT_CATEGORY))))
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `should pass for therapy containing matching drug`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(setOf(drugTreatment(MATCHING_DRUG_NAME, TREATMENT_CATEGORY))))
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `should return undetermined for drug trial treatment with unknown drugs`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatmentNoDrugs("unknown drugs")), isTrial = true)
        assertEvaluation(EvaluationResult.UNDETERMINED, FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `should fail for drug trial treatment with known drugs that don't match`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)), isTrial = true)
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `should fail for non-therapy trial treatments of known category`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(treatment("test", true, setOf(TreatmentCategory.SURGERY))), isTrial = true)
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `should return undetermined for non-therapy trial treatments of unknown category`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(treatment("unknown category", true)), isTrial = true)
        assertEvaluation(EvaluationResult.UNDETERMINED, FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `should pass for drug trial treatment with matching drug`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("match", TreatmentCategory.IMMUNOTHERAPY)), isTrial = true)
        assertEvaluation(EvaluationResult.PASS, FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `should be undetermined for trial with multiple drug therapy treatments including one of unknown drugs`() {
        val knownDrugTherapy = drugTreatment("other", TreatmentCategory.IMMUNOTHERAPY)
        val unknownDrugTherapy = drugTreatmentNoDrugs("unknown drugs")
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(knownDrugTherapy, unknownDrugTherapy), isTrial = true)
        assertEvaluation(EvaluationResult.UNDETERMINED, FUNCTION.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    companion object {
        private const val MATCHING_DRUG_NAME = "match"
        private val TREATMENT_CATEGORY = TreatmentCategory.TARGETED_THERAPY
        private val FUNCTION = HasHadTreatmentWithDrug(
            setOf(Drug(name = MATCHING_DRUG_NAME, category = TREATMENT_CATEGORY, drugTypes = emptySet()))
        )
    }
}