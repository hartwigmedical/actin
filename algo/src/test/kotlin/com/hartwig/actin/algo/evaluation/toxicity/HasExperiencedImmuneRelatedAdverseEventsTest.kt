package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import org.junit.Test

class HasExperiencedImmuneRelatedAdverseEventsTest {
    private val function = HasExperiencedImmuneRelatedAdverseEvents()

    @Test
    fun shouldFailWithNoTreatmentHistory() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun shouldFailWithTreatmentWithOtherCategory() {
        val treatments = listOf(treatmentHistoryEntry(TreatmentCategory.TARGETED_THERAPY))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(treatments)))
    }

    @Test
    fun shouldWarnWithImmunotherapyTreatment() {
        val treatments = listOf(treatmentHistoryEntry(TreatmentCategory.IMMUNOTHERAPY))
        assertEvaluation(EvaluationResult.WARN, function.evaluate(withTreatmentHistory(treatments)))
    }

    private fun withTreatmentHistory(treatmentHistory: List<TreatmentHistoryEntry>): PatientRecord {
        val base = TestPatientFactory.createMinimalTestPatientRecord()
        return base.copy(oncologicalHistory = treatmentHistory)
    }

    private fun treatmentHistoryEntry(category: TreatmentCategory): TreatmentHistoryEntry {
        return TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(treatment(category)))
    }

    private fun treatment(category: TreatmentCategory): Treatment {
        return TreatmentTestFactory.drugTreatment("", category)
    }
}