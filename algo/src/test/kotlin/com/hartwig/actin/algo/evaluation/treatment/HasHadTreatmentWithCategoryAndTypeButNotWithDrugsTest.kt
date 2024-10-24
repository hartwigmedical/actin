package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.washout.WashoutTestFactory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Test

private val MATCHING_CATEGORY = TreatmentCategory.TARGETED_THERAPY
private const val IGNORE_DRUG_NAME = "match"
private val MATCHING_TYPES = setOf(DrugType.ALK_INHIBITOR_GEN_1, DrugType.ALK_INHIBITOR_GEN_2)
private val IGNORE_DRUG_SET = setOf(
    Drug(name = IGNORE_DRUG_NAME, category = MATCHING_CATEGORY, drugTypes = setOf(MATCHING_TYPES.iterator().next()))
)

class HasHadTreatmentWithCategoryAndTypeButNotWithDrugsTest {

    private val functionWithoutTypes = HasHadTreatmentWithCategoryAndTypeButNotWithDrugs(MATCHING_CATEGORY, null, IGNORE_DRUG_SET)
    private val functionWithTypes = HasHadTreatmentWithCategoryAndTypeButNotWithDrugs(MATCHING_CATEGORY, MATCHING_TYPES, IGNORE_DRUG_SET)

    @Test
    fun `Should fail for no treatments`() {
        evaluateFunctions(EvaluationResult.FAIL, withTreatmentHistory(emptyList()))
    }

    @Test
    fun `Should fail for wrong treatment category`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", TreatmentCategory.IMMUNOTHERAPY)))
        evaluateFunctions(EvaluationResult.FAIL, withTreatmentHistoryEntry(treatmentHistoryEntry))
    }

    @Test
    fun `Should fail for treatment with correct category but incorrect type - if type requested`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", MATCHING_CATEGORY, setOf(DrugType.FGFR_INHIBITOR))))
        assertEvaluation(EvaluationResult.FAIL, functionWithTypes.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
    }

    @Test
    fun `Should fail for treatment with correct category and type but ignore drug`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment(IGNORE_DRUG_NAME, MATCHING_CATEGORY, setOf(MATCHING_TYPES.iterator().next())))
        )
        evaluateFunctions(EvaluationResult.FAIL, withTreatmentHistoryEntry(treatmentHistoryEntry))
    }

    @Test
    fun `Should return undetermined for trial treatment`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(treatment("test", true)), isTrial = true)
        evaluateFunctions(EvaluationResult.UNDETERMINED, withTreatmentHistoryEntry(treatmentHistoryEntry))
    }

    @Test
    fun `Should ignore trial matches and fail when looking for unlikely trial categories`() {
        val functionWithoutTypes =
            HasHadTreatmentWithCategoryAndTypeButNotWithDrugs(TreatmentCategory.TRANSPLANTATION, null, IGNORE_DRUG_SET)
        val functionWithTypes =
            HasHadTreatmentWithCategoryAndTypeButNotWithDrugs(TreatmentCategory.TRANSPLANTATION, MATCHING_TYPES, IGNORE_DRUG_SET)
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(treatment("test", false)), isTrial = true)
        assertEvaluation(EvaluationResult.FAIL, functionWithoutTypes.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
        assertEvaluation(EvaluationResult.FAIL, functionWithTypes.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `Should pass for treatment history entry with correct category and type but drug to ignore, and medication entry with correct category and type without drug to ignore`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(
            setOf(drugTreatment(IGNORE_DRUG_NAME, MATCHING_CATEGORY, setOf(MATCHING_TYPES.iterator().next())))
        )
        val medication = WashoutTestFactory.medication().copy(
            drug = Drug(
                name = "test", category = MATCHING_CATEGORY, drugTypes = MATCHING_TYPES
            )
        )
        evaluateFunctions(
            EvaluationResult.PASS,
            TreatmentTestFactory.withTreatmentsAndMedications(listOf(treatmentHistoryEntry), listOf(medication))
        )
    }

    @Test
    fun `Should pass for correct treatment category and type with other drug`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("other drug", MATCHING_CATEGORY, MATCHING_TYPES)))
        evaluateFunctions(EvaluationResult.PASS, withTreatmentHistoryEntry(treatmentHistoryEntry))
    }

    private fun evaluateFunctions(expected: EvaluationResult, record: PatientRecord) {
        assertEvaluation(expected, functionWithTypes.evaluate(record))
        assertEvaluation(expected, functionWithoutTypes.evaluate(record))
    }
}
