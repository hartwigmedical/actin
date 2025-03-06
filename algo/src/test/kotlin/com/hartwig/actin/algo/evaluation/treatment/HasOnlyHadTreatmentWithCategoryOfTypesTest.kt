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
    private val matchingCategory = TreatmentCategory.CHEMOTHERAPY
    private val matchingTypes = setOf(DrugType.PLATINUM_COMPOUND)

    @Test
    fun `Should fail if there are no treatments`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail if there are no treatments that are systemic`() {
        assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                withTreatmentHistory(listOf(treatmentHistoryEntry(setOf(drugTreatment("test", TreatmentCategory.SURGERY, matchingTypes)))))
            )
        )
    }

    @Test
    fun `Should fail if there are treatments of the wrong category`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(makeRecordWithMatchingAndAdditionalEntry(category = TreatmentCategory.HORMONE_THERAPY))
        )
    }

    @Test
    fun `Should pass by ignoring surgery and radiotherapy`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(makeRecordWithMatchingAndAdditionalEntry(category = TreatmentCategory.SURGERY))
        )
    }

    @Test
    fun `Should fail if there are treatments of the wrong type`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(makeRecordWithMatchingAndAdditionalEntry(types = setOf(DrugType.HER2_ANTIBODY)))
        )
    }

    @Test
    fun `Should warn if there are treatments of the unknown type`() {
        assertEvaluation(EvaluationResult.WARN, function.evaluate(makeRecordWithMatchingAndAdditionalEntry(types = emptySet())))
    }

    @Test
    fun `Should pass if all treatments are of the right category and type`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(makeRecordWithMatchingAndAdditionalEntry()))
    }

    private val function = HasOnlyHadTreatmentWithCategoryOfTypes(matchingCategory, matchingTypes)

    private fun makeRecordWithMatchingAndAdditionalEntry(
        category: TreatmentCategory = matchingCategory,
        types: Set<DrugType> = matchingTypes
    ): PatientRecord {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", category, types)))
        val matchingTreatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", matchingCategory, matchingTypes)))
        return withTreatmentHistory(listOf(treatmentHistoryEntry, matchingTreatmentHistoryEntry))
    }
}

