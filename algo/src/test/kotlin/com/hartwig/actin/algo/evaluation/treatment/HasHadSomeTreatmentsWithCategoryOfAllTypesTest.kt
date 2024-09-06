package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.medication.MedicationTestFactory
import com.hartwig.actin.algo.evaluation.washout.WashoutTestFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Test

private val MATCHING_CATEGORY = TreatmentCategory.TARGETED_THERAPY
private val MATCHING_TYPES = setOf(DrugType.HER2_ANTIBODY, DrugType.ANTIBODY_DRUG_CONJUGATE_TARGETED_THERAPY)

class HasHadSomeTreatmentsWithCategoryOfAllTypesTest {

    private val function = HasHadSomeTreatmentsWithCategoryOfAllTypes(MATCHING_CATEGORY, MATCHING_TYPES, 1)

    @Test
    fun `Should pass when treatment history contains treatments of all requested types`() {
        val treatmentHistoryEntry = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(
                    TreatmentTestFactory.drugTreatment("Trastuzumab-Deruxtecan", MATCHING_CATEGORY, MATCHING_TYPES)
                ), 2022, 5
            ),
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(
                    TreatmentTestFactory.drugTreatment(
                        "Doxorubicin",
                        TreatmentCategory.CHEMOTHERAPY,
                        setOf(DrugType.ANTHRACYCLINE)
                    )
                ), 2023, 7
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                (TreatmentTestFactory.withTreatmentHistory(treatmentHistoryEntry))
            )
        )
    }

    @Test
    fun `Should pass for recent correct treatment category with incorrect types but medication with correct types`() {
        val function = HasHadSomeTreatmentsWithCategoryOfAllTypes(MATCHING_CATEGORY, MATCHING_TYPES, 1)
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(
            setOf(TreatmentTestFactory.drugTreatment("test", MATCHING_CATEGORY, setOf(DrugType.ANTI_TISSUE_FACTOR)))
        )
        val medication = WashoutTestFactory.medication().copy(
            drug = Drug(
                name = "", category = MATCHING_CATEGORY, drugTypes = MATCHING_TYPES
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TreatmentTestFactory.withTreatmentsAndMedications(listOf(treatmentHistoryEntry), listOf(medication)))
        )
    }

    @Test
    fun `Should fail when treatment history is empty`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                (TreatmentTestFactory.withTreatmentHistory(emptyList()))
            )
        )
    }

    @Test
    fun `Should fail when treatment history contains only treatments of the wrong category`() {
        val treatmentHistoryEntry = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(
                    TreatmentTestFactory.drugTreatment(
                        "Doxorubicin",
                        TreatmentCategory.CHEMOTHERAPY,
                        setOf(DrugType.ANTHRACYCLINE)
                    )
                ), 2023, 7
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                (TreatmentTestFactory.withTreatmentHistory(treatmentHistoryEntry))
            )
        )
    }

    @Test
    fun `Should fail when treatment history contains treatments of the correct category but of the wrong type`() {
        val treatmentHistoryEntry = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(
                    TreatmentTestFactory.drugTreatment(
                        "Doxorubicin",
                        TreatmentCategory.TARGETED_THERAPY,
                        setOf(DrugType.ALK_INHIBITOR)
                    )
                ), 2023, 7
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                (TreatmentTestFactory.withTreatmentHistory(treatmentHistoryEntry))
            )
        )
    }

    @Test
    fun `Should fail when treatment history contains treatments of the correct category but no treatment is of all requested types`() {
        val treatmentHistoryEntry = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                setOf(
                    TreatmentTestFactory.drugTreatment(
                        "Dacomitinib",
                        TreatmentCategory.TARGETED_THERAPY,
                        setOf(DrugType.HER2_ANTIBODY)
                    )
                ), 2023, 7
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                (TreatmentTestFactory.withTreatmentHistory(treatmentHistoryEntry))
            )
        )
    }

    @Test
    fun `Should fail when medication has drug with correct category but not of all requested types`() {
        val function = HasHadSomeTreatmentsWithCategoryOfAllTypes(MATCHING_CATEGORY, MATCHING_TYPES, 1)
        val medication = WashoutTestFactory.medication().copy(
            drug = Drug(
                name = "", category = MATCHING_CATEGORY, drugTypes = setOf(DrugType.HER2_ANTIBODY)
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MedicationTestFactory.withMedications(listOf(medication)))
        )
    }

    @Test
    fun `Should return fail treatment history contains treatments of the correct category but without DrugType specified`() {
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(
            setOf(
                TreatmentTestFactory.drugTreatment(
                    "test",
                    MATCHING_CATEGORY
                )
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                TreatmentTestFactory.withTreatmentHistory(
                    listOf(treatmentHistoryEntry)
                )
            )
        )
    }

    @Test
    fun `Should return undetermined when trial treatments meet threshold`() {
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(
            setOf(TreatmentTestFactory.drugTreatment("test", MATCHING_CATEGORY)),
            isTrial = true
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withTreatmentHistory(listOf(treatmentHistoryEntry)))
        )
    }

    @Test
    fun `Should ignore trial matches and fail when looking for unlikely trial categories`() {
        val function = HasHadSomeTreatmentsWithCategoryOfAllTypes(TreatmentCategory.TRANSPLANTATION, setOf(OtherTreatmentType.ALLOGENIC), 1)
        val treatmentHistoryEntry = TreatmentTestFactory.treatmentHistoryEntry(
            setOf(TreatmentTestFactory.drugTreatment("test", TreatmentCategory.TRANSPLANTATION)),
            isTrial = true
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(listOf(treatmentHistoryEntry, treatmentHistoryEntry)))
        )
    }
}