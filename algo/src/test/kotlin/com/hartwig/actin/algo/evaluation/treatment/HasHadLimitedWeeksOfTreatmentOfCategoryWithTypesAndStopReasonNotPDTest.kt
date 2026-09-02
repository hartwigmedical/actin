package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse
import org.junit.jupiter.api.Test

class HasHadLimitedWeeksOfTreatmentOfCategoryWithTypesAndStopReasonNotPDTest {

    @Test
    fun `Should fail for empty treatments`() {
        evaluateFunctions(
            EvaluationResult.FAIL,
            TreatmentTestFactory.withTreatmentHistory(emptyList()),
            "No HER2 antibody targeted therapy treatment treatment with PD"
        )
    }

    @Test
    fun `Should fail for wrong category`() {
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(
            setOf(TreatmentTestFactory.drugTreatment("test", TreatmentCategory.RADIOTHERAPY)), stopReason = StopReason.TOXICITY
        )
        evaluateFunctions(
            EvaluationResult.FAIL,
            TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry),
            "No HER2 antibody targeted therapy treatment treatment with PD"
        )
    }

    @Test
    fun `Should fail for right category type with no stop reason but subsequent treatment line within 26 weeks`() {
        val matchingEntry = TreatmentTestFactory.treatmentHistoryEntry(MATCHING_TREATMENT_SET, stopYear = 2020, stopMonth = 6)
        val subsequentEntry = TreatmentTestFactory.treatmentHistoryEntry(
            setOf(TreatmentTestFactory.drugTreatment("other", TreatmentCategory.CHEMOTHERAPY)), startYear = 2020, startMonth = 9
        )
        evaluateFunctions(
            EvaluationResult.FAIL,
            TreatmentTestFactory.withTreatmentHistory(listOf(matchingEntry, subsequentEntry)),
            "HER2 antibody targeted therapy treatment in provided treatments with stop reason PD"
        )
    }

    @Test
    fun `Should fail for right category and type but with PD`() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(
                MATCHING_TREATMENT_SET,
                stopReason = StopReason.PROGRESSIVE_DISEASE,
                bestResponse = TreatmentResponse.MIXED
            )
        evaluateFunctions(
            EvaluationResult.FAIL,
            TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry),
            "HER2 antibody targeted therapy treatment in provided treatments with stop reason PD"
        )
    }

    @Test
    fun `Should return undetermined for right category and missing type`() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(TreatmentTestFactory.drugTreatment("test", MATCHING_CATEGORY)),
                stopReason = StopReason.TOXICITY
            )
        evaluateFunctions(
            EvaluationResult.UNDETERMINED,
            TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry),
            "Unclear if received targeted therapy"
        )
    }

    @Test
    fun `Should return undetermined for right category type and missing stop reason`() {
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(MATCHING_TREATMENT_SET)
        evaluateFunctions(
            EvaluationResult.UNDETERMINED,
            TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry),
            "HER2 antibody targeted therapy treatment but uncertain if there has been PD & unclear nr of weeks  in provided treatments",
            "HER2 antibody targeted therapy treatment but uncertain if there has been PD in provided treatments"
        )
    }

    @Test
    fun `Should pass for right category type with stop reason other than PD with any amount of weeks if weeks not requested`() {
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(
            MATCHING_TREATMENT_SET,
            stopReason = StopReason.TOXICITY,
            startYear = 2022,
            startMonth = 3,
            stopYear = 2024,
            stopMonth = 4
        )

        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            functionWithoutWeeks.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry)),
            "HER2 antibody targeted therapy treatment in provided treatments without stop reason PD"
        )
    }

    @Test
    fun `Should pass for right category type with stop reason other than PD with unknown amount of weeks if weeks not requested`() {
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(
            MATCHING_TREATMENT_SET,
            stopReason = StopReason.TOXICITY,
            startYear = null,
            startMonth = null,
            stopYear = null,
            stopMonth = null
        )

        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            functionWithoutWeeks.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry)),
            "HER2 antibody targeted therapy treatment in provided treatments without stop reason PD"
        )
    }

    @Test
    fun `Should pass for right category type within requested amount of weeks and with stop reason other than PD`() {
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(
            MATCHING_TREATMENT_SET,
            stopReason = StopReason.TOXICITY,
            startYear = 2022,
            startMonth = 3,
            stopYear = 2022,
            stopMonth = 4
        )

        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            functionWithWeeks.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry)),
            "HER2 antibody targeted therapy treatment for less than 6 weeks in provided treatments without stop reason PD"
        )
    }

    @Test
    fun `Should fail for matching treatment when PD is indicated in best response`() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(MATCHING_TREATMENT_SET, bestResponse = TreatmentResponse.PROGRESSIVE_DISEASE)
        evaluateFunctions(
            EvaluationResult.FAIL,
            TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry),
            "HER2 antibody targeted therapy treatment in provided treatments with stop reason PD"
        )
    }

    @Test
    fun `Should return undetermined with trial treatment entry with matching category in history`() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.drugTreatment("test", MATCHING_CATEGORY)), isTrial = true)
        evaluateFunctions(
            EvaluationResult.UNDETERMINED,
            TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry),
            "Unclear if received targeted therapy"
        )
    }

    @Test
    fun `Should ignore trial matches when looking for unlikely trial categories`() {
        val function = HasHadPDFollowingTreatmentWithCategoryOfTypesAndCyclesOrWeeks(
            TreatmentCategory.TRANSPLANTATION, setOf(OtherTreatmentType.ALLOGENIC),
            null, null
        )
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(setOf(TreatmentTestFactory.treatment("test", true)), isTrial = true)
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry)),
            "No allogenic transplantation treatment with PD"
        )
    }

    @Test
    fun `Should return undetermined for right category type and stop reason other than PD when weeks are missing and weeks requested`() {
        val treatmentHistoryEntry =
            TreatmentTestFactory.treatmentHistoryEntry(MATCHING_TREATMENT_SET, stopReason = StopReason.TOXICITY, startYear = null)
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            functionWithWeeks.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry)),
            "HER2 antibody targeted therapy treatment without stop reason PD but unknown nr of weeks in provided treatments"
        )
    }

    @Test
    fun `Should fail for right category type and stop reason other than PD when treatment duration more than max weeks and weeks requested`() {
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(
            MATCHING_TREATMENT_SET,
            stopReason = StopReason.TOXICITY,
            startYear = 2022,
            startMonth = 3,
            stopYear = 2022,
            stopMonth = 6
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            functionWithWeeks.evaluate(TreatmentTestFactory.withTreatmentHistoryEntry(treatmentHistoryEntry)),
            "HER2 antibody targeted therapy treatment in provided treatments with stop reason PD"
        )
    }

    private fun evaluateFunctions(
        expected: EvaluationResult, record: PatientRecord, expectedMessageWithWeeks: String, expectedMessageWithoutWeeks: String? = null
    ) {
        EvaluationAssert.assertEvaluation(expected, functionWithWeeks.evaluate(record), expectedMessageWithWeeks)
        EvaluationAssert.assertEvaluation(expected, functionWithoutWeeks.evaluate(record), expectedMessageWithoutWeeks ?: expectedMessageWithWeeks)
    }

    companion object {
        private val MATCHING_CATEGORY = TreatmentCategory.TARGETED_THERAPY
        private val MATCHING_TYPE_SET = setOf(DrugType.HER2_ANTIBODY)
        private val MATCHING_TREATMENT_SET = setOf(TreatmentTestFactory.drugTreatment("test", MATCHING_CATEGORY, MATCHING_TYPE_SET))
        private val functionWithWeeks =
            HasHadLimitedWeeksOfTreatmentOfCategoryWithTypesAndStopReasonNotPD(MATCHING_CATEGORY, MATCHING_TYPE_SET, 6)
        private val functionWithoutWeeks =
            HasHadLimitedWeeksOfTreatmentOfCategoryWithTypesAndStopReasonNotPD(MATCHING_CATEGORY, MATCHING_TYPE_SET, null)
    }
}