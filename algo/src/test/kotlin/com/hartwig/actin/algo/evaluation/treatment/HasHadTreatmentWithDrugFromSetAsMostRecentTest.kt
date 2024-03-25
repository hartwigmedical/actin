package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.junit.Test

class HasHadTreatmentWithDrugFromSetAsMostRecentTest {

    @Test
    fun `Should fail for empty treatment history`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList()))
        )
    }

    @Test
    fun `Should fail for non-drug treatment`() {
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.treatment("other treatment", false)))
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory))
        )
    }

    @Test
    fun `Should fail for therapy containing other drug`() {
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.drugTreatment("other treatment", TREATMENT_CATEGORY)))
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory))
        )
    }

    @Test
    fun `Should fail for therapy containing matching drug but not most recent line`() {
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.drugTreatment(MATCHING_DRUG_NAME, TREATMENT_CATEGORY)), startYear = 2021),
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.drugTreatment("Other drug", TREATMENT_CATEGORY)), startYear = 2022)
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory))
        )
    }

    @Test
    fun `Should evaluate to undetermined when multiple treatment entries in history of which one contains the target drug with unknown start date`() {
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.drugTreatment(MATCHING_DRUG_NAME, TREATMENT_CATEGORY)), startYear = null),
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.drugTreatment("Other drug", TREATMENT_CATEGORY)), startYear = 2021)
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory))
        )
    }

    @Test
    fun `Should fail if matching drug is not most recent and history contains other random treatment with unknown start date`() {
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.drugTreatment(MATCHING_DRUG_NAME, TREATMENT_CATEGORY)), startYear = 2021),
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.drugTreatment("Other drug", TREATMENT_CATEGORY)), startYear = null),
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.drugTreatment("Drug more recent than target drug", TREATMENT_CATEGORY)), startYear = 2022)
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory))
        )
    }

    @Test
    fun `Should evaluate to undetermined if most recent line is trial without a treatment specified`() {
        val treatmentHistory = listOf(TreatmentTestFactory.treatmentHistoryEntry(treatments = emptySet(), isTrial = true))
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory))
        )
    }

    @Test
    fun `Should evaluate to undetermined if most recent line is trial with treatment containing drug of target category`() {
        val treatmentHistory = listOf(TreatmentTestFactory.treatmentHistoryEntry(
            setOf(TreatmentTestFactory.drugTreatment("Trial drug X", category = TREATMENT_CATEGORY)), isTrial = true)
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory))
        )
    }

    @Test
    fun `Should pass if matching treatment is only history entry and start date is unknown`() {
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.drugTreatment(MATCHING_DRUG_NAME, TREATMENT_CATEGORY)))
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory))
        )
    }

    @Test
    fun `Should pass for therapy containing matching drug`() {
        val treatmentHistory = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.drugTreatment(MATCHING_DRUG_NAME, TREATMENT_CATEGORY)), startYear = 2022)
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, FUNCTION.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentHistory))
        )
    }

    companion object {
        private const val MATCHING_DRUG_NAME = "match"
        private val TREATMENT_CATEGORY = TreatmentCategory.TARGETED_THERAPY
        private val FUNCTION = HasHadTreatmentWithDrugFromSetAsMostRecent(
            setOf(Drug(name = MATCHING_DRUG_NAME, category = TREATMENT_CATEGORY, drugTypes = emptySet()))
        )
    }

}