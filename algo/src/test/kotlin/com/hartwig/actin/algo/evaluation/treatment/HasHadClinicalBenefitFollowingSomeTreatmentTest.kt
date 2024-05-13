package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.Radiotherapy
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
import org.assertj.core.api.Assertions
import org.junit.Test

class HasHadClinicalBenefitFollowingSomeTreatmentTest {

    private val CORRECT_SPECIFIC_TREATMENT = TreatmentTestFactory.treatment("Chemotherapy", true, setOf(TreatmentCategory.CHEMOTHERAPY))
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
    private val WRONG_SPECIFIC_TREATMENT = TreatmentTestFactory.treatment("Radiotherapy", false, setOf(TreatmentCategory.RADIOTHERAPY))
    private val TARGET_CATEGORY = TreatmentCategory.IMMUNOTHERAPY
    private val TARGET_TYPES = setOf(DrugType.ANTI_PD_L1, DrugType.ANTI_PD_1)
    private val CORRECT_CATEGORY_AND_TYPE_TREATMENT = TreatmentTestFactory.drugTreatment("treatment", TARGET_CATEGORY, TARGET_TYPES)
    private val WRONG_CATEGORY_TREATMENT = TreatmentTestFactory.drugTreatment("treatment2", TreatmentCategory.SUPPORTIVE_TREATMENT, TARGET_TYPES)
    private val WRONG_TYPE_TREATMENT = TreatmentTestFactory.drugTreatment("treatment3", TARGET_CATEGORY, setOf(DrugType.TAXANE))
    private val functionWithSpecificTreatment = HasHadClinicalBenefitFollowingSomeTreatment(CORRECT_SPECIFIC_TREATMENT)
    private val functionWithSpecificCategoryAndType =
        HasHadClinicalBenefitFollowingSomeTreatment(category = TARGET_CATEGORY, types = TARGET_TYPES)
    private val functionWithSpecificCategory = HasHadClinicalBenefitFollowingSomeTreatment(category = TARGET_CATEGORY)

    @Test
    fun `Should throw an illegal state exception when specific treatment and category and type not specified in function`() {
        Assertions.assertThatIllegalStateException().isThrownBy {
            HasHadClinicalBenefitFollowingSomeTreatment(null, null, null)
                .evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList()))
        }.withMessage("Treatment not specified")
    }

    @Test
    fun `Should fail if treatment history is empty`() {
            assertEvaluation(EvaluationResult.FAIL, TreatmentTestFactory.withTreatmentHistory(emptyList()))
    }

    @Test
    fun `Should fail if treatment history does not contain target treatment`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(setOf(WRONG_SPECIFIC_TREATMENT)),
            TreatmentTestFactory.treatmentHistoryEntry(setOf(WRONG_CATEGORY_TREATMENT))
        )
        val wrongTypeHistory = listOf(TreatmentTestFactory.treatmentHistoryEntry(setOf(WRONG_TYPE_TREATMENT)))
        assertEvaluation(EvaluationResult.FAIL, TreatmentTestFactory.withTreatmentHistory(history))
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, functionWithSpecificCategoryAndType.evaluate(TreatmentTestFactory.withTreatmentHistory(wrongTypeHistory))
        )
    }

    @Test
    fun `Should pass if treatment history contains specific treatment with best response complete response`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(CORRECT_SPECIFIC_TREATMENT),
                bestResponse = TreatmentResponse.COMPLETE_RESPONSE
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, functionWithSpecificTreatment.evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }

    @Test
    fun `Should pass if treatment history contains treatment of target category and type with best response complete response`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(CORRECT_CATEGORY_AND_TYPE_TREATMENT),
                bestResponse = TreatmentResponse.COMPLETE_RESPONSE
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, functionWithSpecificCategoryAndType.evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }

    @Test
    fun `Should pass if treatment history contains treatment of target category with best response complete response when no type requested`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(CORRECT_CATEGORY_AND_TYPE_TREATMENT),
                bestResponse = TreatmentResponse.COMPLETE_RESPONSE
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, functionWithSpecificCategory.evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }

    @Test
    fun `Should pass if treatment history contains target therapy combined with other therapy with best response complete response`() {
        val history1 = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                TARGET_TREATMENT_WITH_OTHER_CATEGORY_COMBINATION,
                bestResponse = TreatmentResponse.COMPLETE_RESPONSE
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, functionWithSpecificTreatment.evaluate(TreatmentTestFactory.withTreatmentHistory(history1))
        )

        val history2 = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                TARGET_TREATMENT_WITH_SAME_CATEGORY_COMBINATION,
                bestResponse = TreatmentResponse.COMPLETE_RESPONSE
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, functionWithSpecificTreatment.evaluate(TreatmentTestFactory.withTreatmentHistory(history2))
        )
    }

    @Test
    fun `Should pass if treatment history contains target therapy with best response partial response`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(CORRECT_SPECIFIC_TREATMENT),
                bestResponse = TreatmentResponse.PARTIAL_RESPONSE
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, functionWithSpecificTreatment.evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }

    @Test
    fun `Should pass if treatment history contains target therapy with best response remission`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(CORRECT_SPECIFIC_TREATMENT),
                bestResponse = TreatmentResponse.REMISSION
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, functionWithSpecificTreatment.evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }

    @Test
    fun `Should warn if treatment history contains target therapy with best response mixed response`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(CORRECT_CATEGORY_AND_TYPE_TREATMENT),
                bestResponse = TreatmentResponse.MIXED
            ),
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(CORRECT_SPECIFIC_TREATMENT),
                bestResponse = TreatmentResponse.MIXED
            )
        )
        assertEvaluation(EvaluationResult.WARN, TreatmentTestFactory.withTreatmentHistory(history))
    }

    @Test
    fun `Should warn if treatment history contains target therapy with best response stable disease`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(CORRECT_CATEGORY_AND_TYPE_TREATMENT),
                bestResponse = TreatmentResponse.STABLE_DISEASE
            ),
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(CORRECT_SPECIFIC_TREATMENT),
                bestResponse = TreatmentResponse.STABLE_DISEASE
            )
        )
        assertEvaluation(EvaluationResult.WARN, TreatmentTestFactory.withTreatmentHistory(history))
    }

    @Test
    fun `Should evaluate to undetermined if treatment history contains target therapy but no response specified`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(CORRECT_CATEGORY_AND_TYPE_TREATMENT),
                bestResponse = null
            ),
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(CORRECT_SPECIFIC_TREATMENT),
                bestResponse = null
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, TreatmentTestFactory.withTreatmentHistory(history))
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
            functionWithSpecificTreatment.evaluate(TreatmentTestFactory.withTreatmentHistory(history))
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
            functionWithSpecificTreatment.evaluate(TreatmentTestFactory.withTreatmentHistory(history))
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
            functionWithSpecificTreatment.evaluate(TreatmentTestFactory.withTreatmentHistory(history))
        )
    }

    @Test
    fun `Should fail if treatment history contains target therapy but best response progressive disease`() {
        val history = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(CORRECT_CATEGORY_AND_TYPE_TREATMENT),
                bestResponse = TreatmentResponse.PROGRESSIVE_DISEASE
            ),
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(CORRECT_SPECIFIC_TREATMENT),
                bestResponse = TreatmentResponse.PROGRESSIVE_DISEASE
            )
        )
        assertEvaluation(EvaluationResult.FAIL, TreatmentTestFactory.withTreatmentHistory(history))
    }

    private fun assertEvaluation(result: EvaluationResult, record: PatientRecord) {
        EvaluationAssert.assertEvaluation(result, functionWithSpecificTreatment.evaluate(record))
        EvaluationAssert.assertEvaluation(result, functionWithSpecificCategory.evaluate(record))
        EvaluationAssert.assertEvaluation(result, functionWithSpecificCategoryAndType.evaluate(record))
    }
}