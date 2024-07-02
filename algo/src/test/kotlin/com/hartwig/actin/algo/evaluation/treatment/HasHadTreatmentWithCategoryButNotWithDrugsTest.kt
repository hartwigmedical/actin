package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatment
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory.withTreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.junit.Test

class HasHadTreatmentWithCategoryButNotWithDrugsTest {
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
        assertEvaluation(EvaluationResult.FAIL, FUNCTION_WITH_TYPES.evaluate(withTreatmentHistoryEntry(treatmentHistoryEntry)))
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
        val functionWithoutTypes = HasHadTreatmentWithCategoryButNotWithDrugs(TreatmentCategory.TRANSPLANTATION, null, IGNORE_DRUG_SET)
        val functionWithTypes = HasHadTreatmentWithCategoryButNotWithDrugs(TreatmentCategory.TRANSPLANTATION, MATCHING_TYPES, IGNORE_DRUG_SET)
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(treatment("test", false)), isTrial = true)
        assertEvaluation(EvaluationResult.FAIL, functionWithoutTypes.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
        assertEvaluation(EvaluationResult.FAIL, functionWithTypes.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry))))
    }

    @Test
    fun `Should pass for correct treatment category and type with other drug`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("other drug", MATCHING_CATEGORY, MATCHING_TYPES)))
        evaluateFunctions(EvaluationResult.PASS, withTreatmentHistoryEntry(treatmentHistoryEntry))
    }

    private fun evaluateFunctions(expected: EvaluationResult, record: PatientRecord) {
        assertEvaluation(expected, FUNCTION_WITH_TYPES.evaluate(record))
        assertEvaluation(expected, FUNCTION_WITHOUT_TYPES.evaluate(record))
    }

    companion object {
        private val MATCHING_CATEGORY = TreatmentCategory.TARGETED_THERAPY
        private const val IGNORE_DRUG_NAME = "match"
        private val MATCHING_TYPES = setOf(DrugType.ALK_INHIBITOR_GEN_1, DrugType.ALK_INHIBITOR_GEN_2)
        private val IGNORE_DRUG_SET = setOf(
            Drug(name = IGNORE_DRUG_NAME, category = MATCHING_CATEGORY, drugTypes = setOf(MATCHING_TYPES.iterator().next()))
        )
        private val FUNCTION_WITHOUT_TYPES = HasHadTreatmentWithCategoryButNotWithDrugs(MATCHING_CATEGORY, null, IGNORE_DRUG_SET)
        private val FUNCTION_WITH_TYPES = HasHadTreatmentWithCategoryButNotWithDrugs(MATCHING_CATEGORY, MATCHING_TYPES, IGNORE_DRUG_SET)
    }
}
