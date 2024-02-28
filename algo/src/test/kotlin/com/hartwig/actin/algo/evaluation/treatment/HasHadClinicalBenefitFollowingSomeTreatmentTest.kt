package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.Radiotherapy
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import org.junit.Test

class HasHadClinicalBenefitFollowingSomeTreatmentTest {

    private val TARGET_TREATMENT = TreatmentTestFactory.treatment("Chemotherapy", true, setOf(TreatmentCategory.CHEMOTHERAPY))
    private val SIMILAR_DRUG_TO_TARGET_TREATMENT =
        TreatmentTestFactory.treatment("Other chemo", true, setOf(TreatmentCategory.CHEMOTHERAPY))
    private val DRUG_WITH_SAME_CATEGORY_BUT_OTHER_TYPE =
        TreatmentTestFactory.drugTreatment("Paclitaxel", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.TAXANE))
    private val TARGET_TREATMENT_WITH_OTHER_CATEGORY_COMBINATION = setOf(
        TreatmentTestFactory.treatment(
            "Chemotherapy", true, setOf(TreatmentCategory.CHEMOTHERAPY)
        ), Radiotherapy("Radiotherapy")
    )
    private val TARGET_TREATMENT_WITH_SAME_CATEGORY_COMBINATION = setOf(
        TreatmentTestFactory.treatment(
            "Chemotherapy", true, setOf(TreatmentCategory.CHEMOTHERAPY)
        ), TreatmentTestFactory.treatment(
            "Other chemotherapy", true, setOf(TreatmentCategory.CHEMOTHERAPY)
        )
    )
    private val WRONG_TREATMENT = TreatmentTestFactory.treatment("Radiotherapy", false, setOf(TreatmentCategory.RADIOTHERAPY))
    private val function = HasHadClinicalBenefitFollowingSomeTreatment(TARGET_TREATMENT)

    @Test
    fun `Should fail if treatment history is empty`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail if treatment history does not contain target treatment`() {
        val history = listOf(TreatmentTestFactory.treatmentHistoryEntry(setOf(WRONG_TREATMENT)))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withTreatmentHistory(history)))
    }

    @Test
    fun `Should pass if treatment history contains target therapy with best response complete response `() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TARGET_TREATMENT),
                bestResponse = TreatmentResponse.COMPLETE_RESPONSE
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withTreatmentHistory(history)))
    }

    @Test
    fun `Should pass if treatment history contains target therapy combined with other therapy with best response complete response `() {
        val history1 = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                TARGET_TREATMENT_WITH_OTHER_CATEGORY_COMBINATION,
                bestResponse = TreatmentResponse.COMPLETE_RESPONSE
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withTreatmentHistory(history1)))

        val history2 = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                TARGET_TREATMENT_WITH_SAME_CATEGORY_COMBINATION,
                bestResponse = TreatmentResponse.COMPLETE_RESPONSE
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withTreatmentHistory(history2)))
    }

    @Test
    fun `Should pass if treatment history contains target therapy with best response partial response `() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TARGET_TREATMENT),
                bestResponse = TreatmentResponse.PARTIAL_RESPONSE
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withTreatmentHistory(history)))
    }

    @Test
    fun `Should pass if treatment history contains target therapy with best response remission `() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TARGET_TREATMENT),
                bestResponse = TreatmentResponse.REMISSION
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withTreatmentHistory(history)))
    }

    @Test
    fun `Should warn if treatment history contains target therapy with best response mixed response `() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TARGET_TREATMENT),
                bestResponse = TreatmentResponse.MIXED
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.WARN, function.evaluate(TreatmentTestFactory.withTreatmentHistory(history)))
    }

    @Test
    fun `Should warn if treatment history contains target therapy with best response stable disease `() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TARGET_TREATMENT),
                bestResponse = TreatmentResponse.STABLE_DISEASE
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.WARN, function.evaluate(TreatmentTestFactory.withTreatmentHistory(history)))
    }

    @Test
    fun `Should evaluate to undetermined if treatment history contains target therapy but no response specified`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TARGET_TREATMENT),
                bestResponse = null
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }

    @Test
    fun `Should evaluate to undetermined if treatment history contains not exact target therapy but similar drug with best response other than PD`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(SIMILAR_DRUG_TO_TARGET_TREATMENT),
                bestResponse = TreatmentResponse.PARTIAL_RESPONSE
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }

    @Test
    fun `Should fail if treatment history contains similar drug but other type than target drug`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(DRUG_WITH_SAME_CATEGORY_BUT_OTHER_TYPE),
                bestResponse = TreatmentResponse.PARTIAL_RESPONSE
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }

    @Test
    fun `Should fail if treatment history contains not exact target therapy but similar drug but best response PD`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(SIMILAR_DRUG_TO_TARGET_TREATMENT),
                bestResponse = TreatmentResponse.PROGRESSIVE_DISEASE
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }

    @Test
    fun `Should fail if treatment history contains target therapy but best response progressive disease`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TARGET_TREATMENT),
                bestResponse = TreatmentResponse.PROGRESSIVE_DISEASE
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withTreatmentHistory(history)))
    }
}