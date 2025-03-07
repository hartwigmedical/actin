package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Test

class HasHadSystemicTreatmentOnlyOfCategoryOfTypesTest {
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
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", matchingCategory, matchingTypes)))
        val surgery = treatmentHistoryEntry(
            setOf(
                TreatmentTestFactory.treatment(
                    name = "surgery",
                    isSystemic = false,
                    categories = setOf(TreatmentCategory.SURGERY),
                    types = setOf(OtherTreatmentType.DEBULKING_SURGERY)
                )
            )
        )
        val radiotherapy = treatmentHistoryEntry(
            setOf(
                TreatmentTestFactory.treatment(
                    name = "radiotherapy",
                    isSystemic = false,
                    categories = setOf(TreatmentCategory.RADIOTHERAPY),
                    types = setOf(OtherTreatmentType.RADIOFREQUENCY)
                )
            )
        )
        withTreatmentHistory(listOf(treatmentHistoryEntry, surgery))
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, surgery, radiotherapy)))
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
    fun `Should pass undetermined if there are treatments of the unknown type`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(makeRecordWithMatchingAndAdditionalEntry(types = emptySet())))
    }

    @Test
    fun `Should pass if all treatments are of the right category and type`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(makeRecordWithMatchingAndAdditionalEntry()))
    }

    private val function = HasHadSystemicTreatmentOnlyOfCategoryOfTypes(matchingCategory, matchingTypes)

    private fun makeRecordWithMatchingAndAdditionalEntry(
        category: TreatmentCategory = matchingCategory,
        types: Set<DrugType> = matchingTypes
    ): PatientRecord {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", category, types)))
        val matchingTreatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", matchingCategory, matchingTypes)))
        return withTreatmentHistory(listOf(treatmentHistoryEntry, matchingTreatmentHistoryEntry))
    }
}

