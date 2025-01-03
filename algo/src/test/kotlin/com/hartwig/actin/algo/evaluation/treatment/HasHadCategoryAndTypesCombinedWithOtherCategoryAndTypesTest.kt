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

private val CATEGORY_1 = TreatmentCategory.TARGETED_THERAPY
private val DIFFERENT_CATEGORY_1 = TreatmentCategory.IMMUNOTHERAPY
private val TYPES_1 = setOf(DrugType.HER2_ANTIBODY, DrugType.HER3_ANTIBODY)
private val DIFFERENT_TYPES_1 = setOf(DrugType.ANTI_B7H4)
private val CATEGORY_2 = TreatmentCategory.CHEMOTHERAPY
private val TYPES_2 = setOf(DrugType.PLATINUM_COMPOUND)
private val PLATINUM = drugTreatment("other drug", CATEGORY_2, TYPES_2)

class HasHadCategoryAndTypesCombinedWithOtherCategoryAndTypesTest {

    private val function = HasHadCategoryAndTypesCombinedWithOtherCategoryAndTypes(CATEGORY_1, TYPES_1, CATEGORY_2, TYPES_2)

    @Test
    fun `Should fail if treatment history contains no treatments`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail if treatment history contains treatment with first category and types but not combined with second category and types`() {
        val treatmentHistory =
            withTreatmentHistory(
                listOf(
                    treatmentHistoryEntry(setOf(drugTreatment("other drug", CATEGORY_1, TYPES_1))),
                    treatmentHistoryEntry(setOf(PLATINUM))
                )
            )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(treatmentHistory))
    }

    @Test
    fun `Should fail if treatment history contains second category and types but not combined with treatment with first category and types`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(PLATINUM, drugTreatment("wrong name", DIFFERENT_CATEGORY_1)))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail if treatment history entry contains second category and types but combined with treatment with first category and different types than required`() {
        val treatmentHistoryEntry =
            treatmentHistoryEntry(setOf(PLATINUM, drugTreatment("combined", CATEGORY_1, DIFFERENT_TYPES_1)))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail if history contains treatments with correct categories and types but in different instance`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(setOf(PLATINUM)),
            treatmentHistoryEntry(setOf(drugTreatment("combined", CATEGORY_1, DIFFERENT_TYPES_1))),
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    @Test
    fun `Should pass if treatment history contains first category and types combined with treatment with second category and types`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(
                PLATINUM, drugTreatment(
                    "combined", CATEGORY_1,
                    setOf(TYPES_1.first(), DrugType.EGFR_ANTIBODY)
                )
            )
        )
        val function = HasHadCategoryAndTypesCombinedWithOtherCategoryAndTypes(
            CATEGORY_2,
            TYPES_2,
            CATEGORY_1,
            setOf(TYPES_1.first())
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should evaluate to undetermined if treatment with second category and types in history combined with trial without treatments configured`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(PLATINUM, TreatmentTestFactory.treatment("empty trial treatment", isSystemic = true)),
            isTrial = true
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

    @Test
    fun `Should evaluate to undetermined if treatment history contains second category and types combined with first category but types unknown`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(PLATINUM, drugTreatment("combined", CATEGORY_1, emptySet())))
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

    @Test
    fun `Should evaluate to undetermined if treatment history contains first category combined with second category but both types unknown`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(
                drugTreatment("combined 1", CATEGORY_1, emptySet()),
                drugTreatment("combined 2", CATEGORY_2, emptySet())
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }
}