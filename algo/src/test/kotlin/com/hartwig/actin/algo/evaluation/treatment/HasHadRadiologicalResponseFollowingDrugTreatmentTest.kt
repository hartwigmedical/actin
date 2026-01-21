package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse
import org.junit.Test


private const val MATCHING_DRUG_NAME = "match"
private val TREATMENT_CATEGORY = TreatmentCategory.TARGETED_THERAPY

class HasHadRadiologicalResponseFollowingDrugTreatmentTest {

    private val function = HasHadRadiologicalResponseFollowingDrugTreatment(drug(MATCHING_DRUG_NAME))

    private fun drug(name: String): Drug = Drug(name, emptySet(), TREATMENT_CATEGORY)

    private fun drugTreatmentHistoryEntry(drugName: String, bestResponse: TreatmentResponse? = null): TreatmentHistoryEntry =
        treatmentHistoryEntry(
            treatments = setOf(DrugTreatment("treatment", setOf(drug(drugName)))),
            bestResponse = bestResponse)

    @Test
    fun `Should fail for empty treatment history`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail if no matching drugs found`() {
        val treatmentHistory = listOf(drugTreatmentHistoryEntry("other_drug", TreatmentResponse.COMPLETE_RESPONSE))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should fail if matching drugs found and response is progressive disease`() {
        val treatmentHistory = listOf(drugTreatmentHistoryEntry(MATCHING_DRUG_NAME, TreatmentResponse.PROGRESSIVE_DISEASE))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should fail if matching drugs found and response is stable disease`() {
        val treatmentHistory = listOf(drugTreatmentHistoryEntry(MATCHING_DRUG_NAME, TreatmentResponse.STABLE_DISEASE))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should fail if matching drugs found and response is both stable and progressive disease`() {
        val treatmentHistory = listOf(drugTreatmentHistoryEntry(MATCHING_DRUG_NAME, TreatmentResponse.STABLE_DISEASE),
            drugTreatmentHistoryEntry(MATCHING_DRUG_NAME, TreatmentResponse.PROGRESSIVE_DISEASE))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should be undetermined if matching drugs found and response is mixed`() {
        val treatmentHistory = listOf(drugTreatmentHistoryEntry(MATCHING_DRUG_NAME, TreatmentResponse.MIXED))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should be undetermined if matching drugs found and no response available`() {
        val treatmentHistory = listOf(drugTreatmentHistoryEntry(MATCHING_DRUG_NAME))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should pass if treatments of matching drugs found and both positive and negative response available`() {
        val treatmentHistory = listOf(drugTreatmentHistoryEntry(MATCHING_DRUG_NAME, TreatmentResponse.COMPLETE_RESPONSE),
            drugTreatmentHistoryEntry(MATCHING_DRUG_NAME, TreatmentResponse.PROGRESSIVE_DISEASE))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should pass if matching drugs found and response positive`() {
        listOf(
            TreatmentResponse.PARTIAL_RESPONSE,
            TreatmentResponse.COMPLETE_RESPONSE,
            TreatmentResponse.NEAR_COMPLETE_RESPONSE,
            TreatmentResponse.REMISSION
        ).forEach {
            val treatmentHistory = listOf(drugTreatmentHistoryEntry(MATCHING_DRUG_NAME, it))
            assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistory(treatmentHistory)))
        }
    }
}