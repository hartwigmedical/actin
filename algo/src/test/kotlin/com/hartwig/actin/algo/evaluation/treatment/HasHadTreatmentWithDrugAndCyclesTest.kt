package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.washout.WashoutTestFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatmentNoDrugs
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Test

private const val MATCHING_DRUG_NAME = "match"
private val TREATMENT_CATEGORY = TreatmentCategory.TARGETED_THERAPY
private val MATCHING_DRUG_TREATMENT = drugTreatment(MATCHING_DRUG_NAME, TREATMENT_CATEGORY)

class HasHadTreatmentWithDrugAndCyclesTest {

    private val functionWithoutCycles = HasHadTreatmentWithDrugAndCycles(
        setOf(Drug(name = MATCHING_DRUG_NAME, category = TREATMENT_CATEGORY, drugTypes = emptySet())),
        null
    )
    private val functionWithCycles =
        HasHadTreatmentWithDrugAndCycles(setOf(Drug(name = MATCHING_DRUG_NAME, category = TREATMENT_CATEGORY, drugTypes = emptySet())), 3)

    @Test
    fun `Should fail for empty treatment history`() {
        assertEvaluation(EvaluationResult.FAIL, functionWithoutCycles.evaluate(withTreatmentHistory(emptyList())))
        assertEvaluation(EvaluationResult.FAIL, functionWithCycles.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail for non-drug treatment`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(setOf(treatment("other treatment", false))))
        assertEvaluation(EvaluationResult.FAIL, functionWithoutCycles.evaluate(withTreatmentHistory(treatmentHistory)))
        assertEvaluation(EvaluationResult.FAIL, functionWithCycles.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should fail for therapy containing other drug`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(setOf(drugTreatment("other treatment", TREATMENT_CATEGORY))))
        assertEvaluation(EvaluationResult.FAIL, functionWithoutCycles.evaluate(withTreatmentHistory(treatmentHistory)))
        assertEvaluation(EvaluationResult.FAIL, functionWithCycles.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should fail for therapy containing matching drug but not enough cycles`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(setOf(MATCHING_DRUG_TREATMENT), numCycles = 1))
        assertEvaluation(EvaluationResult.FAIL, functionWithCycles.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should fail for maintenance and switch therapies containing matching drug but not enough cycles`() {
        val matchingTreatmentStage = TreatmentTestFactory.treatmentStage(MATCHING_DRUG_TREATMENT, cycles = 1)
        val treatmentHistory = listOf(
            treatmentHistoryEntry(
                maintenanceTreatment = matchingTreatmentStage,
                switchToTreatments = listOf(matchingTreatmentStage)
            )
        )
        assertEvaluation(EvaluationResult.FAIL, functionWithCycles.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should fail for drug trial treatment with known drugs that don't match`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)), isTrial = true)
        assertEvaluation(EvaluationResult.FAIL, functionWithoutCycles.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
        assertEvaluation(EvaluationResult.FAIL, functionWithCycles.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `Should fail for non-therapy trial treatments of known category`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(treatment("test", true, setOf(TreatmentCategory.SURGERY))), isTrial = true)
        assertEvaluation(EvaluationResult.FAIL, functionWithoutCycles.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
        assertEvaluation(EvaluationResult.FAIL, functionWithCycles.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `Should evaluate to undetermined for therapy containing matching drug but undetermined how many cycles`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(setOf(MATCHING_DRUG_TREATMENT), numCycles = null))
        assertEvaluation(EvaluationResult.UNDETERMINED, functionWithCycles.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should evaluate to undetermined for drug trial treatment with unknown drugs`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatmentNoDrugs("unknown drugs")), isTrial = true)
        assertEvaluation(EvaluationResult.UNDETERMINED, functionWithoutCycles.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
        assertEvaluation(EvaluationResult.UNDETERMINED, functionWithCycles.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `Should evaluate to undetermined for non-therapy trial treatments of unknown category`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(treatment("unknown category", true)), isTrial = true)
        assertEvaluation(EvaluationResult.UNDETERMINED, functionWithoutCycles.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
        assertEvaluation(EvaluationResult.UNDETERMINED, functionWithCycles.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `Should evaluate to undetermined for drug trial treatment with matching drug`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("match", TreatmentCategory.IMMUNOTHERAPY)), isTrial = true)
        assertEvaluation(EvaluationResult.UNDETERMINED, functionWithCycles.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `Should evaluate to undetermined for therapy containing other drug but medication containing drug`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(setOf(drugTreatment("other treatment", TREATMENT_CATEGORY))))
        val medication = WashoutTestFactory.medication().copy(
            drug = Drug(
                name = "match", category = TREATMENT_CATEGORY, drugTypes = setOf(DrugType.HER2_ANTIBODY)
            )
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            functionWithCycles.evaluate(TreatmentTestFactory.withTreatmentsAndMedications(treatmentHistory, listOf(medication)))
        )
    }

    @Test
    fun `Should evaluate to undetermined for trial with multiple drug therapy treatments including one of unknown drugs`() {
        val knownDrugTherapy = drugTreatment("other", TreatmentCategory.IMMUNOTHERAPY)
        val unknownDrugTherapy = drugTreatmentNoDrugs("unknown drugs")
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(knownDrugTherapy, unknownDrugTherapy), isTrial = true)
        assertEvaluation(EvaluationResult.UNDETERMINED, functionWithoutCycles.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
        assertEvaluation(EvaluationResult.UNDETERMINED, functionWithCycles.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `Should evaluate to undetermined for maintenance therapy containing matching drug but undetermined how many cycles`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(
                maintenanceTreatment = TreatmentTestFactory.treatmentStage(
                    MATCHING_DRUG_TREATMENT,
                    cycles = null
                )
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, functionWithCycles.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should evaluate to undetermined for switch therapy containing matching drug but undetermined how many cycles`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(
                switchToTreatments = listOf(
                    TreatmentTestFactory.treatmentStage(
                        MATCHING_DRUG_TREATMENT,
                        cycles = null
                    )
                )
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, functionWithCycles.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should pass for therapy containing matching drug`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(setOf(MATCHING_DRUG_TREATMENT)))
        assertEvaluation(EvaluationResult.PASS, functionWithoutCycles.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should pass for therapy containing matching drug and enough cycles`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(setOf(MATCHING_DRUG_TREATMENT), numCycles = 3))
        assertEvaluation(EvaluationResult.PASS, functionWithCycles.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should pass for drug trial treatment with matching drug`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("match", TreatmentCategory.IMMUNOTHERAPY)), isTrial = true)
        assertEvaluation(EvaluationResult.PASS, functionWithoutCycles.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `Should pass for therapy containing other drug but medication containing drug`() {
        val treatmentHistory = listOf(treatmentHistoryEntry(setOf(drugTreatment("other treatment", TREATMENT_CATEGORY))))
        val medication = WashoutTestFactory.medication().copy(
            drug = Drug(
                name = "match", category = TREATMENT_CATEGORY, drugTypes = setOf(DrugType.HER2_ANTIBODY)
            )
        )
        assertEvaluation(
            EvaluationResult.PASS,
            functionWithoutCycles.evaluate(TreatmentTestFactory.withTreatmentsAndMedications(treatmentHistory, listOf(medication)))
        )
    }

    @Test
    fun `Should pass for maintenance therapy containing matching drug and enough cycles`() {
        val treatmentHistory =
            listOf(treatmentHistoryEntry(maintenanceTreatment = TreatmentTestFactory.treatmentStage(MATCHING_DRUG_TREATMENT, cycles = 3)))
        assertEvaluation(EvaluationResult.PASS, functionWithCycles.evaluate(withTreatmentHistory(treatmentHistory)))
        assertEvaluation(EvaluationResult.PASS, functionWithoutCycles.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should pass for switch therapy containing matching drug and enough cycles`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(
                switchToTreatments = listOf(
                    TreatmentTestFactory.treatmentStage(
                        MATCHING_DRUG_TREATMENT,
                        cycles = 3
                    )
                )
            )
        )
        assertEvaluation(EvaluationResult.PASS, functionWithCycles.evaluate(withTreatmentHistory(treatmentHistory)))
        assertEvaluation(EvaluationResult.PASS, functionWithoutCycles.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should pass for maintenance and switch therapies containing matching drug and enough cycles`() {
        val matchingTreatmentStage = TreatmentTestFactory.treatmentStage(MATCHING_DRUG_TREATMENT, cycles = 2)
        val treatmentHistory = listOf(
            treatmentHistoryEntry(
                maintenanceTreatment = matchingTreatmentStage,
                switchToTreatments = listOf(matchingTreatmentStage)
            )
        )
        assertEvaluation(EvaluationResult.PASS, functionWithCycles.evaluate(withTreatmentHistory(treatmentHistory)))
    }
}