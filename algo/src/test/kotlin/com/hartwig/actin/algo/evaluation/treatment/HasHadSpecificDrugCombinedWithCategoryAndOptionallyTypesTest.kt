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

class HasHadSpecificDrugCombinedWithCategoryAndOptionallyTypesTest {

    @Test
    fun `Should fail if treatment history contains no treatments`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(emptyList())))
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
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(treatmentHistory))
    }

    @Test
    fun `Should fail if treatment history contains treatment with target drug but not combined with treatment with required category`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(
                MATCHING_DRUG_TREATMENT,
                drugTreatment("wrong name", DIFFERENT_CATEGORY)
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should pass if combination of target drug and treatment with target category in history if function requires no types`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(MATCHING_DRUG_TREATMENT, drugTreatment("combined", MATCHING_CATEGORY)))
        val function = HasHadSpecificDrugCombinedWithCategoryAndOptionallyTypes(
            MATCHING_DRUG_TREATMENT.drugs.first(), MATCHING_CATEGORY, emptySet()
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
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
        val function = HasHadSpecificDrugCombinedWithCategoryAndOptionallyTypes(
            MATCHING_DRUG_TREATMENT.drugs.first(), MATCHING_CATEGORY, setOf(MATCHING_TYPES.first())
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should pass requested drug and requested combination treatment are both of same type and category`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(MATCHING_DRUG_TREATMENT, drugTreatment("combined", MATCHING_CATEGORY, MATCHING_TYPES))
        )
        val function = HasHadSpecificDrugCombinedWithCategoryAndOptionallyTypes(
            MATCHING_DRUG_TREATMENT.drugs.first(), MATCHING_CATEGORY, MATCHING_TYPES
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should evaluate to undetermined if requested drug in history combined with trial without treatments configured`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(
                MATCHING_DRUG_TREATMENT,
                TreatmentTestFactory.treatment("empty trial treatment", isSystemic = true)
            ), isTrial = true
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

    @Test
    fun `Should evaluate to undetermined if treatment history entry does not have any treatments specified`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(emptySet(), isTrial = true)
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry))
        )
    }

    @Test
    fun `Should fail if types required but none match treatment history`() {
        val treatmentHistoryEntry =
            treatmentHistoryEntry(setOf(MATCHING_DRUG_TREATMENT, drugTreatment("combined", MATCHING_CATEGORY, DIFFERENT_TYPES)))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail if history contains treatment with correct name and other with correct category and type but in different instance`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(setOf(MATCHING_DRUG_TREATMENT)),
            treatmentHistoryEntry(setOf(drugTreatment("combined", MATCHING_CATEGORY, DIFFERENT_TYPES))),
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, FUNCTION.evaluate(withTreatmentHistory(treatmentHistory)))
    }

    companion object {
        private val MATCHING_CATEGORY = TreatmentCategory.CHEMOTHERAPY
        private val DIFFERENT_CATEGORY = TreatmentCategory.IMMUNOTHERAPY
        private val MATCHING_TYPES = setOf(DrugType.HER2_ANTIBODY, DrugType.HER3_ANTIBODY)
        private val DIFFERENT_TYPES = setOf(DrugType.ABL_INHIBITOR)
        private val MATCHING_DRUG_TREATMENT = drugTreatment("Target drug", MATCHING_CATEGORY, MATCHING_TYPES)

        private val FUNCTION = HasHadSpecificDrugCombinedWithCategoryAndOptionallyTypes(
            MATCHING_DRUG_TREATMENT.drugs.first(), MATCHING_CATEGORY, MATCHING_TYPES
        )
    }
}