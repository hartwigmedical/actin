package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Test

class HasOnlyHadTreatmentWithCategoryOfTypesTest {
    private val function = HasOnlyHadTreatmentWithCategoryOfTypes(TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.EGFR_INHIBITOR_GEN_3))

    @Test
    fun `Should fail if there are no treatments`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail if there are treatments of the wrong category`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(makeRecord(category = TreatmentCategory.SURGERY)))
    }

    @Test
    fun `Should fail if there are treatments of the wrong type`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(makeRecord(types = setOf(DrugType.HER2_ANTIBODY))))
    }

    @Test
    fun `Should warn if there are treatments of the null type`() {
        assertEvaluation(EvaluationResult.WARN, function.evaluate(makeRecord(types = emptySet())))
    }

    @Test
    fun `Should pass if all treatments are of the right category and type`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(makeRecord()))
    }
}

private fun makeRecord(
    category: TreatmentCategory = TreatmentCategory.CHEMOTHERAPY,
    types: Set<DrugType> = setOf(DrugType.EGFR_INHIBITOR_GEN_3)
): PatientRecord {
    val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", category, types)))
    return withTreatmentHistory(listOf(treatmentHistoryEntry))
}