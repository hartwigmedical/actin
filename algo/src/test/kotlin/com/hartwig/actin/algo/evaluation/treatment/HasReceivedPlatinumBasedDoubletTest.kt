package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.junit.Test

class HasReceivedPlatinumBasedDoubletTest {

    private val platinumChemoDrug =
        TreatmentTestFactory.drugTreatment("Carboplatin", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.PLATINUM_COMPOUND))
    private val otherChemoDrug =
        TreatmentTestFactory.drugTreatment("Pemetrexed", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.ANTIMETABOLITE))

    @Test
    fun `Should pass if treatment history contains platinum doublet`() {
        val history = listOf(TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(platinumChemoDrug, otherChemoDrug)))

        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            HasReceivedPlatinumBasedDoublet().evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }

    @Test
    fun `Should fail if treatment history contains platinum chemotherapy and other chemotherapy but different treatment instances`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(otherChemoDrug)),
            TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(platinumChemoDrug))
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            HasReceivedPlatinumBasedDoublet().evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }

    @Test
    fun `Should fail if treatment history contains other doublet than platinum doublet`() {
        val history = listOf(TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(otherChemoDrug, otherChemoDrug)))

        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            HasReceivedPlatinumBasedDoublet().evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }

    @Test
    fun `Should fail if treatment history is empty`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            HasReceivedPlatinumBasedDoublet().evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList()))
        )
    }
}