package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.washout.WashoutTestFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentsAndMedications
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import org.junit.Test

private const val MATCHING_DRUG_NAME = "match"
private val TREATMENT_CATEGORY = TreatmentCategory.TARGETED_THERAPY

class HasHadTreatmentWithDrugAndDoseReductionTest {

    private val functionWithDrug =
        HasHadTreatmentWithDrugAndDoseReduction(Drug(name = MATCHING_DRUG_NAME, category = TREATMENT_CATEGORY, drugTypes = emptySet()),)

    @Test
    fun `Should fail for empty treatment history`() {
        assertEvaluation(EvaluationResult.FAIL, functionWithDrug.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail for no matching drugs in treatment history`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(
            setOf(DrugTreatment("treatment",
                    setOf(Drug(name = "other_drug", category = TREATMENT_CATEGORY, drugTypes = emptySet()))))))
        assertEvaluation(EvaluationResult.FAIL, functionWithDrug.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should fail for no matching drugs in medication`() {
        val treatmentHistory = emptyList<TreatmentHistoryEntry>()
        val medication = WashoutTestFactory.medication().copy(
            drug = Drug(name = "other_drug", category = TREATMENT_CATEGORY, drugTypes = emptySet()))
        assertEvaluation(EvaluationResult.FAIL, functionWithDrug.evaluate(withTreatmentsAndMedications(treatmentHistory, listOf(medication))))
    }

    @Test
    fun `Undetermined if matching drugs in treatment history`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(
            setOf(DrugTreatment("treatment",
                setOf(Drug(name = MATCHING_DRUG_NAME, category = TREATMENT_CATEGORY, drugTypes = emptySet()))))))
        assertEvaluation(EvaluationResult.UNDETERMINED, functionWithDrug.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Undetermined if matching drugs in medication`() {
        val treatmentHistory = emptyList<TreatmentHistoryEntry>()
        val medication = WashoutTestFactory.medication().copy(
            drug = Drug(name = MATCHING_DRUG_NAME, category = TREATMENT_CATEGORY, drugTypes = emptySet()))
        assertEvaluation(EvaluationResult.UNDETERMINED, functionWithDrug.evaluate(withTreatmentsAndMedications(treatmentHistory, listOf(medication))))
    }
}