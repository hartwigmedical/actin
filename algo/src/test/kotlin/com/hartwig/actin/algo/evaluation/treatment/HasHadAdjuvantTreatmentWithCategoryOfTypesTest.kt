package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import org.junit.jupiter.api.Test

class HasHadAdjuvantTreatmentWithCategoryOfTypesTest {

    @Test
    fun shouldFailForEmptyTreatmentList() {
        assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(emptyList())), "Not received adjuvant HER2 antibody")
    }

    @Test
    fun shouldFailForAdjuvantTreatmentNotMatchingCategoryOrType() {
        assertResultForCategoryAndTypeAndIntent(
            EvaluationResult.FAIL,
            TreatmentCategory.IMMUNOTHERAPY,
            setOf(DrugType.ANTI_ANDROGEN),
            setOf(Intent.ADJUVANT),
            "Not received adjuvant HER2 antibody"
        )
    }

    @Test
    fun shouldFailForNonAdjuvantTreatmentMatchingCategoryAndType() {
        assertResultForCategoryAndTypeAndIntent(
            EvaluationResult.FAIL,
            WARN_CATEGORY,
            MATCHING_TYPE_SET,
            emptySet(),
            "Not received adjuvant HER2 antibody"
        )
    }

    @Test
    fun shouldFailForNeoadjuvantTreatmentMatchingCategoryAndType() {
        assertResultForCategoryAndTypeAndIntent(
            EvaluationResult.FAIL,
            WARN_CATEGORY,
            MATCHING_TYPE_SET,
            setOf(Intent.NEOADJUVANT),
            "Not received adjuvant HER2 antibody"
        )
    }

    @Test
    fun shouldFailForAdjuvantTreatmentMatchingCategoryWithOtherType() {
        assertResultForCategoryAndTypeAndIntent(
            EvaluationResult.FAIL,
            WARN_CATEGORY,
            setOf(DrugType.ANTI_TISSUE_FACTOR),
            setOf(Intent.ADJUVANT),
            "Not received adjuvant HER2 antibody"
        )
    }

    @Test
    fun shouldWarnForAdjuvantTreatmentMatchingCategoryWithUnspecifiedType() {
        assertResultForCategoryAndTypeAndIntent(
            EvaluationResult.WARN,
            WARN_CATEGORY,
            emptySet(),
            setOf(Intent.ADJUVANT),
            "Received adjuvant targeted therapy but not of specific type)"
        )
    }

    @Test
    fun shouldPassForAdjuvantTreatmentMatchingCategoryAndType() {
        assertResultForCategoryAndTypeAndIntent(
            EvaluationResult.PASS,
            WARN_CATEGORY,
            MATCHING_TYPE_SET,
            setOf(Intent.ADJUVANT),
            "Received adjuvant drug therapy"
        )
    }

    @Test
    fun shouldPassForNeoadjuvantAndAdjuvantTreatmentMatchingCategoryAndType() {
        assertResultForCategoryAndTypeAndIntent(
            EvaluationResult.PASS,
            WARN_CATEGORY,
            MATCHING_TYPE_SET,
            setOf(Intent.NEOADJUVANT, Intent.ADJUVANT),
            "Received adjuvant drug therapy"
        )
    }

    private fun assertResultForCategoryAndTypeAndIntent(
        expectedResult: EvaluationResult,
        category: TreatmentCategory,
        types: Set<DrugType>,
        intents: Set<Intent>,
        expectedMessage: String
    ) {
        val treatment = drugTreatment("drug therapy", category, types)
        val record = withTreatmentHistoryEntry(treatmentHistoryEntry(setOf(treatment), intents = intents))
        assertEvaluation(expectedResult, FUNCTION.evaluate(record), expectedMessage)
    }

    companion object {
        private val WARN_CATEGORY = TreatmentCategory.TARGETED_THERAPY
        private val MATCHING_TYPE_SET = setOf(DrugType.HER2_ANTIBODY)
        private val FUNCTION = HasHadAdjuvantTreatmentWithCategoryOfTypes(MATCHING_TYPE_SET, WARN_CATEGORY)
    }
}

