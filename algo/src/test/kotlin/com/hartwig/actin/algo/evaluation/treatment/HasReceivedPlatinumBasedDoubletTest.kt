package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentStage
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.Radiotherapy
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Test

class HasReceivedPlatinumBasedDoubletTest {

    private val platinumDoublet =
        DrugTreatment(
            name = "Carboplatin+Pemetrexed",
            drugs = setOf(
                Drug(name = "Carboplatin", category = TreatmentCategory.CHEMOTHERAPY, drugTypes = setOf(DrugType.PLATINUM_COMPOUND)),
                Drug(name = "Pemetrexed", category = TreatmentCategory.CHEMOTHERAPY, drugTypes = setOf(DrugType.ANTIMETABOLITE))
            )
        )
    private val platinumDoubletWithImmuno =
        platinumDoublet.copy(
            name = platinumDoublet.name.plus("+Pembrolizumab"),
            drugs = platinumDoublet.drugs
                .plus(Drug(name = "Pembrolizumab", category = TreatmentCategory.IMMUNOTHERAPY, drugTypes = setOf(DrugType.ANTI_PD_1)))
        )
    private val platinumTriplet =
        platinumDoublet.copy(
            name = platinumDoublet.name.plus("+Paclitaxel"),
            drugs = platinumDoublet.drugs
                .plus(Drug(name = "Paclitaxel", category = TreatmentCategory.CHEMOTHERAPY, drugTypes = setOf(DrugType.TAXANE)))
        )

    private val platinumChemoDrug =
        TreatmentTestFactory.drugTreatment("Carboplatin", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.PLATINUM_COMPOUND))
    private val otherChemoDrug =
        TreatmentTestFactory.drugTreatment("Paclitaxel", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.TAXANE))
    private val otherCategoryDrug =
        TreatmentTestFactory.drugTreatment("Nivolumab", TreatmentCategory.IMMUNOTHERAPY)

    @Test
    fun `Should pass if treatment history contains platinum doublet`() {
        val history = listOf(TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(platinumDoublet)))

        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            HasReceivedPlatinumBasedDoublet().evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }

    @Test
    fun `Should pass if treatment history contains platinum doublet in combination with treatment of other category`() {
        val history = listOf(TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(platinumDoubletWithImmuno)))

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
                    treatments = setOf(platinumDoublet), maintenanceTreatment = treatmentStage(otherChemoDrug)
                )
            )

        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            HasReceivedPlatinumBasedDoublet().evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }

    @Test
    fun `Should warn if treatment history contains platinum triplet`() {
        val history = listOf(TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(platinumTriplet)))

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
                        platinumTriplet,
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