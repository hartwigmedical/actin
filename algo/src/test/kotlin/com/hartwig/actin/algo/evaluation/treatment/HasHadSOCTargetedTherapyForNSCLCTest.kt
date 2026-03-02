package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.DrugType.Companion.NSCLC_SOC_TARGETED_THERAPY_DRUG_TYPES
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.jupiter.api.Test

class HasHadSOCTargetedTherapyForNSCLCTest {
    
    private val genesToIgnore = setOf("EGFR")
    private val functionNotIgnoringGenes = HasHadSOCTargetedTherapyForNSCLC(emptySet())
    private val functionIgnoringGenes = HasHadSOCTargetedTherapyForNSCLC(genesToIgnore)
    private val correctDrugType = NSCLC_SOC_TARGETED_THERAPY_DRUG_TYPES.values.flatten().first()
    private val correctTreatment = drugTreatment("Correct", TreatmentCategory.TARGETED_THERAPY, setOf(correctDrugType))
    private val wrongTreatment = drugTreatment("Correct", TreatmentCategory.TARGETED_THERAPY, setOf(DrugType.IDO1_INHIBITOR))

    @Test
    fun `Should fail for empty treatment history`(){
        assertEvaluationForAllFunctions(withTreatmentHistory(emptyList()), EvaluationResult.FAIL)
    }

    @Test
    fun `Should fail for other category than targeted therapy`(){
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(drugTreatment("Wrong category treatment", TreatmentCategory.IMMUNOTHERAPY, setOf(correctDrugType)))
            )
        )
        assertEvaluationForAllFunctions(withTreatmentHistory(treatmentHistory), EvaluationResult.FAIL)
    }

    @Test
    fun `Should fail for wrong drug type`(){
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(setOf(wrongTreatment))
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
                    NSCLC_SOC_TARGETED_THERAPY_DRUG_TYPES[genesToIgnore.first()]!!
                    )
                )
            )
        )
        assertEvaluation(EvaluationResult.FAIL, functionIgnoringGenes.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should pass for SOC targeted therapy in history and gene not in genesToIgnore list`(){
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(setOf(correctTreatment))
        )
        assertEvaluationForAllFunctions(withTreatmentHistory(treatmentHistory), EvaluationResult.PASS)
    }

    private fun assertEvaluationForAllFunctions(record: PatientRecord, expected: EvaluationResult) {
        assertEvaluation(expected, functionNotIgnoringGenes.evaluate(record))
        assertEvaluation(expected, functionIgnoringGenes.evaluate(record))
    }
}