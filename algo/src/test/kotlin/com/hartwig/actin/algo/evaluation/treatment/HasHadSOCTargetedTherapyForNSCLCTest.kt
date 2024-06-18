package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.junit.Test

class HasHadSOCTargetedTherapyForNSCLCTest {
    private val genesToIgnore = listOf("EGFR")
    private val functionNotIgnoringGenes = HasHadSOCTargetedTherapyForNSCLC(emptyList())
    private val functionIgnoringGenes = HasHadSOCTargetedTherapyForNSCLC(genesToIgnore)
    private fun assertEvaluationForAllFunctions(record: PatientRecord, expected: EvaluationResult) {
        assertEvaluation(expected, functionNotIgnoringGenes.evaluate(record))
        assertEvaluation(expected, functionIgnoringGenes.evaluate(record))
    }
    private val CORRECT_DRUG_TYPE = HasHadSOCTargetedTherapyForNSCLC.NSCLC_SOC_TARGETED_THERAPY_DRUG_TYPES.values.flatten().first()
    private val CORRECT_TREATMENT = drugTreatment("Correct", TreatmentCategory.TARGETED_THERAPY, setOf(CORRECT_DRUG_TYPE))
    private val WRONG_TREATMENT = drugTreatment("Correct", TreatmentCategory.TARGETED_THERAPY, setOf(DrugType.IDO1_INHIBITOR))

    @Test
    fun `Should fail for empty treatment history`(){
        assertEvaluationForAllFunctions(withTreatmentHistory(emptyList()), EvaluationResult.FAIL)
    }

    @Test
    fun `Should fail for other category than targeted therapy`(){
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(drugTreatment("Wrong category treatment", TreatmentCategory.IMMUNOTHERAPY, setOf(CORRECT_DRUG_TYPE)))
            )
        )
        assertEvaluationForAllFunctions(withTreatmentHistory(treatmentHistory), EvaluationResult.FAIL)
    }

    @Test
    fun `Should fail for wrong drug type`(){
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(setOf(WRONG_TREATMENT))
        )
        assertEvaluationForAllFunctions(withTreatmentHistory(treatmentHistory), EvaluationResult.FAIL)
    }

    @Test
    fun `Should fail for correct drug type but gene in genesToIgnore list`(){
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(
                    drugTreatment(
                    "Osimertinib",
                    TreatmentCategory.TARGETED_THERAPY,
                    HasHadSOCTargetedTherapyForNSCLC.NSCLC_SOC_TARGETED_THERAPY_DRUG_TYPES[genesToIgnore.first()]!!
                    )
                )
            )
        )
        assertEvaluation(EvaluationResult.FAIL, functionIgnoringGenes.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should pass for SOC targeted therapy in history and gene not in genesToIgnore list`(){
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(setOf(CORRECT_TREATMENT))
        )
        assertEvaluationForAllFunctions(withTreatmentHistory(treatmentHistory), EvaluationResult.PASS)
    }
}