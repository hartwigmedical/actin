package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.junit.Test

private val TARGET_CATEGORY = TreatmentCategory.CHEMOTHERAPY
private val TARGET_TYPES = setOf(DrugType.PLATINUM_COMPOUND, DrugType.ANTHRACYCLINE)

class IsEligibleForTreatmentOfCategoryAndTypeTest {

    private val function = IsEligibleForTreatmentOfCategoryAndType(TARGET_CATEGORY, TARGET_TYPES)

    @Test
    fun `Should evaluate to undetermined for empty treatment history`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should evaluate to undetermined when treatment history only contains entries with wrong category or type`() {
        val treatmentList = listOf(
            createTreatmentHistoryEntry(TreatmentCategory.IMMUNOTHERAPY, setOf(DrugType.PLATINUM_COMPOUND)),
            createTreatmentHistoryEntry(TreatmentCategory.CHEMOTHERAPY, setOf(DrugType.ALKYLATING_AGENT))
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentList)))
    }

    @Test
    fun `Should evaluate to undetermined for entry with correct category but no type configured`() {
        val treatmentList = listOf(createTreatmentHistoryEntry(TreatmentCategory.CHEMOTHERAPY, emptySet()))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatmentList)))
    }

    @Test
    fun `Should warn when treatment history contains entry with correct category and type`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(TreatmentTestFactory.withTreatmentHistory(listOf(createTreatmentHistoryEntry(TARGET_CATEGORY, TARGET_TYPES))))
        )
    }

    private fun createTreatmentHistoryEntry(category: TreatmentCategory, types: Set<DrugType>) =
        TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(TreatmentTestFactory.drugTreatment("drug therapy", category, types)))
}