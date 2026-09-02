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
import org.junit.jupiter.api.Test

private val MATCHING_CATEGORY = TreatmentCategory.CHEMOTHERAPY
private val DIFFERENT_CATEGORY = TreatmentCategory.IMMUNOTHERAPY
private val MATCHING_TYPES = setOf(DrugType.HER2_ANTIBODY, DrugType.HER3_ANTIBODY)
private val DIFFERENT_TYPES = setOf(DrugType.ABL_INHIBITOR)
private val MATCHING_DRUG_TREATMENT = drugTreatment("Target drug", MATCHING_CATEGORY, MATCHING_TYPES)

class HasHadSpecificDrugCombinedWithCategoryAndOptionallyTypesAsLineWithCyclesTest {

    private val functionWithoutCycles = HasHadSpecificDrugCombinedWithCategoryAndOptionallyTypesAsLineWithCycles(
        MATCHING_DRUG_TREATMENT.drugs.first(),
        MATCHING_CATEGORY,
        MATCHING_TYPES,
        null,
        null
    )

    @Test
    fun `Should fail if treatment history contains no treatments`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            functionWithoutCycles.evaluate(withTreatmentHistory(emptyList())),
            "No combined therapy with target drug and HER2 antibody and HER3 antibody chemotherapy in provided treatments"
        )
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
        assertEvaluation(
            EvaluationResult.FAIL,
            functionWithoutCycles.evaluate(treatmentHistory),
            "No combined therapy with target drug and HER2 antibody and HER3 antibody chemotherapy in provided treatments"
        )
    }

    @Test
    fun `Should fail if treatment history contains treatment with target drug but not combined with treatment with required category`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(
                MATCHING_DRUG_TREATMENT,
                drugTreatment("wrong name", DIFFERENT_CATEGORY)
            )
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            functionWithoutCycles.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)),
            "No combined therapy with target drug and HER2 antibody and HER3 antibody chemotherapy in provided treatments"
        )
    }

    @Test
    fun `Should pass if combination of target drug and treatment with target category in history if function requires no types and line`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(MATCHING_DRUG_TREATMENT, drugTreatment("combined", MATCHING_CATEGORY)))
        val function = HasHadSpecificDrugCombinedWithCategoryAndOptionallyTypesAsLineWithCycles(
            MATCHING_DRUG_TREATMENT.drugs.first(), MATCHING_CATEGORY, null, null, null
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)),
            "combined therapy with target drug and chemotherapy in provided treatments"
        )
    }

    @Test
    fun `Should pass if single type is requested and treatment is of multiple types of which one is the requested and no line requested`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(
                MATCHING_DRUG_TREATMENT, drugTreatment(
                    "combined", MATCHING_CATEGORY,
                    setOf(MATCHING_TYPES.first(), DrugType.EGFR_ANTIBODY)
                )
            )
        )
        val function = HasHadSpecificDrugCombinedWithCategoryAndOptionallyTypesAsLineWithCycles(
            MATCHING_DRUG_TREATMENT.drugs.first(), MATCHING_CATEGORY, setOf(MATCHING_TYPES.first()), null, null
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)),
            "combined therapy with target drug and HER2 antibody chemotherapy in provided treatments"
        )
    }

    @Test
    fun `Should pass requested drug and requested combination treatment are both of same type and category and no line requested`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(MATCHING_DRUG_TREATMENT, drugTreatment("combined", MATCHING_CATEGORY, MATCHING_TYPES))
        )
        val function = HasHadSpecificDrugCombinedWithCategoryAndOptionallyTypesAsLineWithCycles(
            MATCHING_DRUG_TREATMENT.drugs.first(), MATCHING_CATEGORY, MATCHING_TYPES, null, null
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)),
            "combined therapy with target drug and HER2 antibody and HER3 antibody chemotherapy in provided treatments"
        )
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
            functionWithoutCycles.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)),
            "Undetermined if combined therapy with target drug and HER2 antibody and HER3 antibody chemotherapy based on provided treatments"
        )
    }

    @Test
    fun `Should evaluate to undetermined if treatment history entry does not have any treatments specified`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(emptySet(), isTrial = true)
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            functionWithoutCycles.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)),
            "Undetermined if combined therapy with target drug and HER2 antibody and HER3 antibody chemotherapy based on provided treatments"
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
        val function = HasHadSpecificDrugCombinedWithCategoryAndOptionallyTypesAsLineWithCycles(
            MATCHING_DRUG_TREATMENT.drugs.first(), MATCHING_CATEGORY, setOf(MATCHING_TYPES.first()), 2, null
        )
        val result = function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            result,
            "combined therapy with target drug and HER2 antibody chemotherapy in provided treatments but unknown if in line 2"
        )
    }

    @Test
    fun `Should warn if patient received drug combined with requested category and type but with less cycles than required`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(MATCHING_DRUG_TREATMENT, drugTreatment("combined", MATCHING_CATEGORY, MATCHING_TYPES)), numCycles = 2
        )
        val function = HasHadSpecificDrugCombinedWithCategoryAndOptionallyTypesAsLineWithCycles(
            MATCHING_DRUG_TREATMENT.drugs.first(), MATCHING_CATEGORY, MATCHING_TYPES, null, 4
        )
        val result = function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry))
        assertEvaluation(
            EvaluationResult.WARN,
            result,
            "combined therapy with target drug and HER2 antibody and HER3 antibody chemotherapy in provided treatments but with less than 4 cycles"
        )
    }

    @Test
    fun `Should pass if patient received drug combined with requested category and type with at least required cycles`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(MATCHING_DRUG_TREATMENT, drugTreatment("combined", MATCHING_CATEGORY, MATCHING_TYPES)), numCycles = 4
        )
        val function = HasHadSpecificDrugCombinedWithCategoryAndOptionallyTypesAsLineWithCycles(
            MATCHING_DRUG_TREATMENT.drugs.first(), MATCHING_CATEGORY, MATCHING_TYPES, null, 4
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)),
            "combined therapy with target drug and HER2 antibody and HER3 antibody chemotherapy and at least 4 cycles in provided treatments"
        )
    }

    @Test
    fun `Should evaluate to undetermined if cycles required but cycles unknown in treatment history`() {
        val treatmentHistoryEntry =
            treatmentHistoryEntry(setOf(MATCHING_DRUG_TREATMENT, drugTreatment("combined", MATCHING_CATEGORY, MATCHING_TYPES)))
        val function = HasHadSpecificDrugCombinedWithCategoryAndOptionallyTypesAsLineWithCycles(
            MATCHING_DRUG_TREATMENT.drugs.first(), MATCHING_CATEGORY, MATCHING_TYPES, null, 4
        )
        val result = function.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            result,
            "Undetermined if combined therapy with target drug and HER2 antibody and HER3 antibody chemotherapy and at least 4 cycles based on provided treatments"
        )
    }

    @Test
    fun `Should fail if types required but none match treatment history`() {
        val treatmentHistoryEntry =
            treatmentHistoryEntry(setOf(MATCHING_DRUG_TREATMENT, drugTreatment("combined", MATCHING_CATEGORY, DIFFERENT_TYPES)))
        assertEvaluation(
            EvaluationResult.FAIL,
            functionWithoutCycles.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)),
            "No combined therapy with target drug and HER2 antibody and HER3 antibody chemotherapy in provided treatments"
        )
    }

    @Test
    fun `Should fail if history contains treatment with correct name and other with correct category and type but in different instance`() {
        val treatmentHistory = listOf(
            treatmentHistoryEntry(setOf(MATCHING_DRUG_TREATMENT)),
            treatmentHistoryEntry(setOf(drugTreatment("combined", MATCHING_CATEGORY, DIFFERENT_TYPES))),
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            functionWithoutCycles.evaluate(withTreatmentHistory(treatmentHistory)),
            "No combined therapy with target drug and HER2 antibody and HER3 antibody chemotherapy in provided treatments"
        )
    }
}