package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Test

class HasHadSystemicTreatmentOnlyOfCategoryOfTypesTest {
    private val matchingCategory = TreatmentCategory.CHEMOTHERAPY
    private val matchingTypes = setOf(DrugType.ANTI_ANDROGEN)
    private val surgery = treatmentHistoryEntry(
        setOf(
            treatment(
                "surgery",
                false,
                setOf(TreatmentCategory.SURGERY),
                setOf(OtherTreatmentType.DEBULKING_SURGERY)
            )
        )
    )
    private val ablation = treatmentHistoryEntry(
        setOf(
            treatment(
                "ablation",
                false,
                setOf(TreatmentCategory.ABLATION),
                setOf(OtherTreatmentType.RADIOFREQUENCY)
            )
        )
    )

    @Test
    fun `Should fail if there are no treatments`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail if there are no treatments that are systemic`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(listOf(surgery))))
    }

    @Test
    fun `Should fail if there are treatments of the wrong category`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(makeRecordWithMatchingAndAdditionalEntry(category = TreatmentCategory.HORMONE_THERAPY))
        )
    }

    @Test
    fun `Should pass when only treatment history entry with correct category and type ignoring non systemic treatments`() {
        val treatmentHistoryEntry = treatmentHistoryEntry(setOf(drugTreatment("test", matchingCategory, matchingTypes)))
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(withTreatmentHistory(listOf(treatmentHistoryEntry, surgery, ablation)))
        )
    }

    @Test
    fun `Should fail if treatment history with correct category but wrong type`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(makeRecordWithMatchingAndAdditionalEntry(types = setOf(DrugType.ALKYLATING_AGENT)))
        )
    }

    @Test
    fun `Should pass undetermined if there are treatments of unknown type`() {
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

