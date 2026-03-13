package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse
import org.junit.jupiter.api.Test

private val MATCHING_CATEGORY = TreatmentCategory.CHEMOTHERAPY
private val DIFFERENT_CATEGORY = TreatmentCategory.IMMUNOTHERAPY
private val MATCHING_TYPES = setOf(DrugType.HER2_ANTIBODY, DrugType.HER3_ANTIBODY)
private val DIFFERENT_TYPES = setOf(DrugType.ABL_INHIBITOR)
private val MATCHING_DRUG_TREATMENT = drugTreatment("Target drug", MATCHING_CATEGORY, MATCHING_TYPES)

class HasHadPDFollowingSpecificDrugCombinedWithCategoryAndTypesAndMinimumWeeksTest {

    val function = HasHadPDFollowingSpecificDrugCombinedWithCategoryAndTypesAndMinimumWeeks(
        MATCHING_DRUG_TREATMENT.drugs.first(),
        MATCHING_CATEGORY,
        MATCHING_TYPES,
        6
    )

    @Test
    fun `Should fail for empty treatments`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail if history contains treatment with right category and type but not combined with target drug`() {
        val treatmentHistory =
            withTreatmentHistory(
                listOf(
                    treatmentHistoryEntry(setOf(drugTreatment("wrong drug", MATCHING_CATEGORY, emptySet()))),
                    treatmentHistoryEntry(setOf(drugTreatment("other drug", MATCHING_CATEGORY, MATCHING_TYPES)))
                )
            )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(treatmentHistory))
    }

    @Test
    fun `Should fail if treatment history contains treatment with target drug but not combined with treatment with required category`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(
                MATCHING_DRUG_TREATMENT,
                drugTreatment("wrong name", DIFFERENT_CATEGORY)
            )
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail for combination of target drug and treatment with target category and type in history but no PD`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(MATCHING_DRUG_TREATMENT, drugTreatment("combined", MATCHING_CATEGORY)),
            stopReason = StopReason.TOXICITY
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail for combination of target drug and treatment with target category and type and best response PD and insufficient weeks`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(MATCHING_DRUG_TREATMENT, drugTreatment("combined", MATCHING_CATEGORY)),
            stopReason = StopReason.PROGRESSIVE_DISEASE,
            startYear = 2022,
            startMonth = 3,
            stopYear = 2022,
            stopMonth = 3
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail if types required but none match treatment history`() {
        val treatmentHistoryEntry =
            treatmentHistoryEntry(setOf(MATCHING_DRUG_TREATMENT, drugTreatment("combined", MATCHING_CATEGORY, DIFFERENT_TYPES)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail if history contains treatment with correct name and other with correct category and type but in different instance`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(setOf(MATCHING_DRUG_TREATMENT)),
            treatmentHistoryEntry(setOf(drugTreatment("combined", MATCHING_CATEGORY, DIFFERENT_TYPES))),
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should fail with requested drug and requested combination treatment are both of same type and category and PD but insufficient weeks`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(MATCHING_DRUG_TREATMENT, drugTreatment("combined", MATCHING_CATEGORY, MATCHING_TYPES)),
            stopReason = StopReason.PROGRESSIVE_DISEASE,
            startYear = 2022,
            startMonth = 3,
            stopYear = 2022,
            stopMonth = 3
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should return undetermined for combination of target drug and treatment with target category and type but missing stop reason`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(MATCHING_DRUG_TREATMENT, drugTreatment("combined", MATCHING_CATEGORY)))
        val function = HasHadPDFollowingSpecificDrugCombinedWithCategoryAndTypesAndMinimumWeeks(
            MATCHING_DRUG_TREATMENT.drugs.first(), MATCHING_CATEGORY, emptySet(), 6
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should return undetermined for combination of target drug and treatment with target category and type and stop reason PD when weeks unknown`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(MATCHING_DRUG_TREATMENT, drugTreatment("combined", MATCHING_CATEGORY)),
            stopReason = StopReason.PROGRESSIVE_DISEASE,
            startYear = 2022,
            startMonth = 3
        )
        val function = HasHadPDFollowingSpecificDrugCombinedWithCategoryAndTypesAndMinimumWeeks(
            MATCHING_DRUG_TREATMENT.drugs.first(), MATCHING_CATEGORY, emptySet(), 6
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should return undetermined if requested drug in history combined with trial without treatments configured`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(
                MATCHING_DRUG_TREATMENT,
                TreatmentTestFactory.treatment("empty trial treatment", isSystemic = true)
            ), isTrial = true
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

    @Test
    fun `Should return undetermined for correct specific combination received twice with PD below the requested amount of weeks but together this exceeds the min weeks`() {
        val treatmentHistoryEntry1 = treatmentHistoryEntry(
            setOf(MATCHING_DRUG_TREATMENT, drugTreatment("combined", MATCHING_CATEGORY)),
            stopReason = StopReason.PROGRESSIVE_DISEASE,
            startYear = 2017,
            startMonth = 3,
            stopYear = 2017,
            stopMonth = 3
        )
        val treatmentHistoryEntry2 = treatmentHistoryEntry(
            setOf(MATCHING_DRUG_TREATMENT, drugTreatment("combined", MATCHING_CATEGORY)),
            stopReason = StopReason.PROGRESSIVE_DISEASE,
            startYear = 2018,
            startMonth = 3,
            stopYear = 2018,
            stopMonth = 3
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry1, treatmentHistoryEntry2)))
        )
    }

    @Test
    fun `Should pass for combination of target drug and treatment with target category and type and stop reason PD and sufficient weeks`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(MATCHING_DRUG_TREATMENT, drugTreatment("combined", MATCHING_CATEGORY)),
            stopReason = StopReason.PROGRESSIVE_DISEASE,
            startYear = 2022,
            startMonth = 3,
            stopYear = 2022,
            stopMonth = 8
        )
        val function = HasHadPDFollowingSpecificDrugCombinedWithCategoryAndTypesAndMinimumWeeks(
            MATCHING_DRUG_TREATMENT.drugs.first(), MATCHING_CATEGORY, emptySet(), 6
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should pass for combination of target drug and treatment with target category and type and best response PD and sufficient weeks`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(MATCHING_DRUG_TREATMENT, drugTreatment("combined", MATCHING_CATEGORY)),
            bestResponse = TreatmentResponse.PROGRESSIVE_DISEASE,
            startYear = 2022,
            startMonth = 3,
            stopYear = 2022,
            stopMonth = 8
        )
        val function = HasHadPDFollowingSpecificDrugCombinedWithCategoryAndTypesAndMinimumWeeks(
            MATCHING_DRUG_TREATMENT.drugs.first(), MATCHING_CATEGORY, emptySet(), 6
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should pass if single type is requested and treatment is of multiple types of which one is the requested and PD and sufficient weeks`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(
                MATCHING_DRUG_TREATMENT,
                drugTreatment(
                    "combined", MATCHING_CATEGORY,
                    setOf(MATCHING_TYPES.first(), DrugType.EGFR_ANTIBODY)
                ),
            ), stopReason = StopReason.PROGRESSIVE_DISEASE, startYear = 2022,
            startMonth = 3,
            stopYear = 2022,
            stopMonth = 8
        )
        val function = HasHadPDFollowingSpecificDrugCombinedWithCategoryAndTypesAndMinimumWeeks(
            MATCHING_DRUG_TREATMENT.drugs.first(), MATCHING_CATEGORY, setOf(MATCHING_TYPES.first()), 6
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should pass requested drug and requested combination treatment are both of same type and category and PD and sufficient weeks`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(MATCHING_DRUG_TREATMENT, drugTreatment("combined", MATCHING_CATEGORY, MATCHING_TYPES)),
            stopReason = StopReason.PROGRESSIVE_DISEASE,
            startYear = 2022,
            startMonth = 3,
            stopYear = 2022,
            stopMonth = 8
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }
}