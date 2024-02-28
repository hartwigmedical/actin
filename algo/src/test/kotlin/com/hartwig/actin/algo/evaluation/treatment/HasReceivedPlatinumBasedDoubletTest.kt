package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatmentStage
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.Radiotherapy
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.junit.Test

class HasReceivedPlatinumBasedDoubletTest {

    private val platinumChemoDrug =
        TreatmentTestFactory.drugTreatment("Carboplatin", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.PLATINUM_COMPOUND))
    private val otherChemoDrug =
        TreatmentTestFactory.drugTreatment("Pemetrexed", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.ANTIMETABOLITE))
    private val anotherChemoDrug =
        TreatmentTestFactory.drugTreatment("Paclitaxel", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.TAXANE))
    private val otherCategoryDrug =
        TreatmentTestFactory.drugTreatment("Nivolumab", TreatmentCategory.IMMUNOTHERAPY)

    @Test
    fun `Should pass if treatment history contains platinum doublet`() {
        val history = listOf(TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(platinumChemoDrug, otherChemoDrug)))

        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            HasReceivedPlatinumBasedDoublet().evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }

    @Test
    fun `Should pass if treatment history contains platinum doublet in combination with treatment of other category`() {
        val history =
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    treatments = setOf(
                        platinumChemoDrug,
                        otherChemoDrug,
                        otherCategoryDrug
                    )
                )
            )

        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            HasReceivedPlatinumBasedDoublet().evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }

    @Test
    fun `Should pass if treatment history contains platinum doublet and maintenance of another chemotherapy drug thereafter`() {
        val history =
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    treatments = setOf(
                        platinumChemoDrug,
                        otherChemoDrug
                    ),
                    maintenanceTreatment = treatmentStage(anotherChemoDrug)
                )
            )

        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            HasReceivedPlatinumBasedDoublet().evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }

    @Test
    fun `Should warn if treatment history contains platinum triplet`() {
        val history =
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    treatments = setOf(
                        platinumChemoDrug,
                        otherChemoDrug,
                        anotherChemoDrug
                    )
                )
            )

        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            HasReceivedPlatinumBasedDoublet().evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }

    @Test
    fun `Should warn if treatment history contains platinum triplet combined with other category drug`() {
        val history =
            listOf(
                TreatmentTestFactory.treatmentHistoryEntry(
                    treatments = setOf(
                        platinumChemoDrug,
                        otherChemoDrug,
                        anotherChemoDrug,
                        otherCategoryDrug
                    )
                )
            )

        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            HasReceivedPlatinumBasedDoublet().evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }

    @Test
    fun `Should fail if treatment history contains platinum chemotherapy and other chemotherapy but different treatment instances`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(otherChemoDrug, otherChemoDrug)),
            TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(platinumChemoDrug, Radiotherapy("Radiotherapy")))
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            HasReceivedPlatinumBasedDoublet().evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }

    @Test
    fun `Should fail if treatment history contains platinum monotherapy and other chemotherapy monotherapy as maintenance`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                treatments = setOf(platinumChemoDrug),
                maintenanceTreatment = treatmentStage(otherChemoDrug)
            ),
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