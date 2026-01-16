package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Test

private const val MATCHING_DRUG_NAME = "match"
private val TREATMENT_CATEGORY = TreatmentCategory.CHEMOTHERAPY

class HasHadChemoradiotherapyWithDrugAndCyclesTest {

    @Test
    fun `Should fail for empty treatment history`() {
        assertEvaluation(EvaluationResult.FAIL, functionWithCycles.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Pass if radiochemotherapy and matching drugs and sufficient cycles`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(
            treatments = setOf(
                drugTreatment("match", TreatmentCategory.CHEMOTHERAPY),
                drugTreatment("mismatch", TreatmentCategory.RADIOTHERAPY)),
            numCycles = 5)
        )

        assertEvaluation(EvaluationResult.PASS, functionWithCycles.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Undetermined if radiochemotherapy and matching drugs and unknown cycles`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(
            treatments = setOf(
                drugTreatment("match", TreatmentCategory.CHEMOTHERAPY),
                drugTreatment("mismatch", TreatmentCategory.RADIOTHERAPY)))
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, functionWithCycles.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    // TODO: UNKNOWN DRUGS -> UNDETERMINED?
    @Test
    fun `Undetermined if radiochemotherapy and unknown drugs and sufficient cycles`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(
            treatments = setOf(treatment("name", isSystemic = false, categories = setOf(TreatmentCategory.CHEMOTHERAPY, TreatmentCategory.RADIOTHERAPY))),
            numCycles = 5)
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, functionWithCycles.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    // TODO: UNKNOWN DRUGS -> UNDETERMINED?
    @Test
    fun `Undetermined if radiochemotherapy and unknown drugs and unknown cycles`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(
            treatments = setOf(treatment("name", isSystemic = false, categories = setOf(TreatmentCategory.CHEMOTHERAPY, TreatmentCategory.RADIOTHERAPY))))
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, functionWithCycles.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Fail if radiochemotherapy and no matching drugs and unknown cycles`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(
            treatments = setOf(
                drugTreatment("mismatch", TreatmentCategory.CHEMOTHERAPY),
                drugTreatment("mismatch", TreatmentCategory.RADIOTHERAPY)))
        )
        assertEvaluation(EvaluationResult.FAIL, functionWithCycles.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Fail if radiochemotherapy and no matching drugs and sufficient cycles`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(
            treatments = setOf(
                drugTreatment("mismatch", TreatmentCategory.CHEMOTHERAPY),
                drugTreatment("mismatch", TreatmentCategory.RADIOTHERAPY)),
            numCycles = 5)
        )
        assertEvaluation(EvaluationResult.FAIL, functionWithCycles.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Fail if radiochemotherapy and no matching drugs and insufficient cycles`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(
            treatments = setOf(
                drugTreatment("mismatch", TreatmentCategory.CHEMOTHERAPY),
                drugTreatment("mismatch", TreatmentCategory.RADIOTHERAPY)),
            numCycles = 1)
        )
        assertEvaluation(EvaluationResult.FAIL, functionWithCycles.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Fail if radiochemotherapy and matching drugs and insufficient cycles`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(
            treatments = setOf(
                drugTreatment("match", TreatmentCategory.CHEMOTHERAPY),
                drugTreatment("mismatch", TreatmentCategory.RADIOTHERAPY)),
            numCycles = 1)
        )
        assertEvaluation(EvaluationResult.FAIL, functionWithCycles.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Fail if no radiochemotherapy and matching drugs and sufficient cycles`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(
            treatments = setOf(
                drugTreatment("match", TreatmentCategory.IMMUNOTHERAPY),
                drugTreatment("mismatch", TreatmentCategory.RADIOTHERAPY)),
            numCycles = 1)
        )
        assertEvaluation(EvaluationResult.FAIL, functionWithCycles.evaluate(withTreatmentHistory(treatmentHistory)))
    }




    private val functionWithCycles =
        HasHadChemoradiotherapyWithDrugAndCycles(setOf(Drug(name = MATCHING_DRUG_NAME, category = TREATMENT_CATEGORY, drugTypes = emptySet())), 3)
    
}