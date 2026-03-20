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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

private val MATCHING_CATEGORY = TreatmentCategory.CHEMOTHERAPY
private val DIFFERENT_CATEGORY = TreatmentCategory.IMMUNOTHERAPY
private val MATCHING_TYPES = setOf(DrugType.HER2_ANTIBODY, DrugType.HER3_ANTIBODY)
private val DIFFERENT_TYPES = setOf(DrugType.ABL_INHIBITOR)
private val MATCHING_DRUG_TREATMENT = drugTreatment("Target drug", MATCHING_CATEGORY, MATCHING_TYPES)

class HasHadSpecificDrugCombinedWithCategoryAndOptionallyTypesAsLineTest {

    private val function = HasHadSpecificDrugCombinedWithCategoryAndOptionallyTypesAsLine(
        MATCHING_DRUG_TREATMENT.drugs.first(),
        MATCHING_CATEGORY,
        MATCHING_TYPES,
        null
    )

    @Test
    fun `Should fail if treatment history contains no treatments`() {
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
    fun `Should pass if combination of target drug and treatment with target category in history if function requires no types`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(MATCHING_DRUG_TREATMENT, drugTreatment("combined", MATCHING_CATEGORY)))
        val function = HasHadSpecificDrugCombinedWithCategoryAndOptionallyTypesAsLine(
            MATCHING_DRUG_TREATMENT.drugs.first(), MATCHING_CATEGORY, emptySet(), null
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should pass if single type is requested and treatment is of multiple types of which one is the requested`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(
                MATCHING_DRUG_TREATMENT, drugTreatment(
                    "combined", MATCHING_CATEGORY,
                    setOf(MATCHING_TYPES.first(), DrugType.EGFR_ANTIBODY)
                )
            )
        )
        val function = HasHadSpecificDrugCombinedWithCategoryAndOptionallyTypesAsLine(
            MATCHING_DRUG_TREATMENT.drugs.first(), MATCHING_CATEGORY, setOf(MATCHING_TYPES.first()), null
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should pass requested drug and requested combination treatment are both of same type and category`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(MATCHING_DRUG_TREATMENT, drugTreatment("combined", MATCHING_CATEGORY, MATCHING_TYPES))
        )
        val function = HasHadSpecificDrugCombinedWithCategoryAndOptionallyTypesAsLine(
            MATCHING_DRUG_TREATMENT.drugs.first(), MATCHING_CATEGORY, MATCHING_TYPES, null
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should evaluate to undetermined if requested drug in history combined with trial without treatments configured`() {
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
    fun `Should evaluate to undetermined if treatment history entry does not have any treatments specified`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(emptySet(), isTrial = true)
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

    @Test
    fun `Should evaluate to undetermined if patient received drug combined with requested category and type but line is requested`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(
                MATCHING_DRUG_TREATMENT, drugTreatment(
                    "combined", MATCHING_CATEGORY,
                    setOf(MATCHING_TYPES.first(), DrugType.EGFR_ANTIBODY)
                )
            )
        )
        val function = HasHadSpecificDrugCombinedWithCategoryAndOptionallyTypesAsLine(
            MATCHING_DRUG_TREATMENT.drugs.first(), MATCHING_CATEGORY, setOf(MATCHING_TYPES.first()), 2
        )
        val result = function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry))
        assertEvaluation(EvaluationResult.UNDETERMINED, result)
        assertThat(result.undeterminedMessagesStrings()).containsExactly("Has received combined therapy with target drug and HER2 antibody chemotherapy but unknown if in line 2")
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
}