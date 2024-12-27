package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Test

private val MATCHING_CATEGORY = TreatmentCategory.TARGETED_THERAPY
private val DIFFERENT_CATEGORY = TreatmentCategory.IMMUNOTHERAPY
private val MATCHING_TYPES = setOf(DrugType.HER2_ANTIBODY, DrugType.HER3_ANTIBODY)
private val DIFFERENT_TYPES = setOf(DrugType.ANTI_B7H4)
private val PLATINUM_CHEMOTHERAPY = drugTreatment("Platinum", TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.PLATINUM_COMPOUND))

class HasHadPlatinumBasedChemotherapyCombinedWithCategoryAndOptionallyTypesTest {

    private val function = HasHadPlatinumBasedChemotherapyCombinedWithCategoryAndOptionallyTypes(MATCHING_CATEGORY, MATCHING_TYPES)

    @Test
    fun `Should fail if treatment history contains no treatments`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail if history contains treatment with right category and type but not combined with platinum chemotherapy`() {
        val treatmentHistory =
            withTreatmentHistory(
                listOf(
                    treatmentHistoryEntry(setOf(PLATINUM_CHEMOTHERAPY)),
                    treatmentHistoryEntry(setOf(drugTreatment("other drug", MATCHING_CATEGORY, MATCHING_TYPES)))
                )
            )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(treatmentHistory))
    }

    @Test
    fun `Should fail if treatment history contains platinum chemotherapy but not combined with treatment with required category`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(
                PLATINUM_CHEMOTHERAPY,
                drugTreatment("wrong name", DIFFERENT_CATEGORY)
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail if types required but none match treatment history`() {
        val treatmentHistoryEntry =
            treatmentHistoryEntry(setOf(PLATINUM_CHEMOTHERAPY, drugTreatment("combined", MATCHING_CATEGORY, DIFFERENT_TYPES)))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail if history contains treatment with correct name and other with correct category and type but in different instance`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(setOf(PLATINUM_CHEMOTHERAPY)),
            treatmentHistoryEntry(setOf(drugTreatment("combined", MATCHING_CATEGORY, DIFFERENT_TYPES))),
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should pass if combination of platinum chemotherapy and treatment with target category in history if function requires no types`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(PLATINUM_CHEMOTHERAPY, drugTreatment("combined", MATCHING_CATEGORY)))
        val function = HasHadPlatinumBasedChemotherapyCombinedWithCategoryAndOptionallyTypes(MATCHING_CATEGORY, emptySet())
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should pass if single type is requested and treatment is of multiple types of which one is the requested`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(
                PLATINUM_CHEMOTHERAPY, drugTreatment(
                    "combined", MATCHING_CATEGORY,
                    setOf(MATCHING_TYPES.first(), DrugType.EGFR_ANTIBODY)
                )
            )
        )
        val function = HasHadSpecificDrugCombinedWithCategoryAndOptionallyTypes(
            PLATINUM_CHEMOTHERAPY.drugs.first(),
            MATCHING_CATEGORY,
            setOf(MATCHING_TYPES.first())
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should evaluate to undetermined if platinum chemotherapy in history combined with trial without treatments configured`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(
                PLATINUM_CHEMOTHERAPY,
                TreatmentTestFactory.treatment("empty trial treatment", isSystemic = true)
            ), isTrial = true
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

    @Test
    fun `Should evaluate to undetermined if treatment history entry does not have any treatments specified`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(emptySet(), isTrial = true)
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }
}