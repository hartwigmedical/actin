package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import org.junit.Test

class HasAcquiredResistanceToAnyDrugTest {

    val TARGET_DRUG_TREATMENT =
        TreatmentTestFactory.drugTreatment("Osimertinib", TreatmentCategory.TARGETED_THERAPY, setOf(DrugType.EGFR_INHIBITOR))
    val WRONG_DRUG_TREATMENT =
        TreatmentTestFactory.drugTreatment("Alectinib", TreatmentCategory.TARGETED_THERAPY, setOf(DrugType.ALK_INHIBITOR))
    val function = HasAcquiredResistanceToAnyDrug(TARGET_DRUG_TREATMENT.drugs)

    @Test
    fun `Should pass if target drug in treatment history with stop reason progressive disease`() {
        val history = TreatmentTestFactory.treatmentHistoryEntry(
            treatments = setOf(TARGET_DRUG_TREATMENT),
            stopReason = StopReason.PROGRESSIVE_DISEASE
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(history)))
    }

    @Test
    fun `Should pass if target drug in treatment history with best response progressive disease`() {
        val history = TreatmentTestFactory.treatmentHistoryEntry(
            treatments = setOf(TARGET_DRUG_TREATMENT),
            bestResponse = TreatmentResponse.PROGRESSIVE_DISEASE
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(history)))
    }

    @Test
    fun `Should pass for matching drug in switch to treatment with stop reason PD`() {
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(
            setOf(WRONG_DRUG_TREATMENT),
            stopReason = StopReason.PROGRESSIVE_DISEASE,
            switchToTreatments = listOf(TreatmentTestFactory.treatmentStage(TARGET_DRUG_TREATMENT))
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

    @Test
    fun `Should evaluate to undetermined if target drug in treatment history with stop reason toxicity`() {
        val history = TreatmentTestFactory.treatmentHistoryEntry(
            treatments = setOf(TARGET_DRUG_TREATMENT),
            stopReason = StopReason.TOXICITY
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(history))
        )
    }

    @Test
    fun `Should evaluate to undetermined if target drug in treatment history with stop reason and best response null`() {
        val history = TreatmentTestFactory.treatmentHistoryEntry(
            treatments = setOf(TARGET_DRUG_TREATMENT),
            stopReason = null,
            bestResponse = null
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(history))
        )
    }

    @Test
    fun `Should evaluate to undetermined if target drug in treatment history with stop reason null and best response partial response`() {
        val history = TreatmentTestFactory.treatmentHistoryEntry(
            treatments = setOf(TARGET_DRUG_TREATMENT),
            stopReason = null,
            bestResponse = TreatmentResponse.PARTIAL_RESPONSE
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(history))
        )
    }

    @Test
    fun `Should evaluate to undetermined with uncategorized trial treatment entry in history`() {
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(setOf(DrugTreatment("test", emptySet())), isTrial = true)
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

    @Test
    fun `Should fail if target drug not in treatment history`() {
        val history = TreatmentTestFactory.treatmentHistoryEntry(
            treatments = setOf(WRONG_DRUG_TREATMENT),
            stopReason = StopReason.PROGRESSIVE_DISEASE
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(history))
        )
    }

    @Test
    fun `Should fail if oncological history is empty`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList()))
        )
    }
}