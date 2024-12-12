package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory.createMinimalTestWGSPatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasExperiencedImmuneRelatedAdverseEventsTest {
    private val function = HasExperiencedImmuneRelatedAdverseEvents()

    @Test
    fun `Should fail with no treatmentHistory`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail with treatment with other category`() {
        val treatments = listOf(treatmentHistoryEntry(TreatmentCategory.TARGETED_THERAPY))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(treatments)))
    }

    @Test
    fun `Should warn with prior immunotherapy treatment and stop reason toxicity`() {
        val treatments = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                treatments = setOf(treatment(TreatmentCategory.IMMUNOTHERAPY)), stopReason = StopReason.TOXICITY
            )
        )
        val evaluation = function.evaluate(withTreatmentHistory(treatments))
        assertEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.warnGeneralMessages).containsExactly(
            "Patient may have experienced immunotherapy related adverse events (prior immunotherapy with stop reason toxicity)"
        )
    }

    @Test
    fun `Should warn with prior immunotherapy treatment with immunotherapy intolerance`() {
        val treatments = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                treatments = setOf(TreatmentTestFactory.drugTreatment("Nivolumab", TreatmentCategory.IMMUNOTHERAPY)),
                stopReason = StopReason.PROGRESSIVE_DISEASE
            )
        )
        val intolerance = Intolerance(
            name = "Nivolumab induced pneumonitis",
            treatmentCategories = setOf(TreatmentCategory.IMMUNOTHERAPY),
            icdCode = IcdCode("", null)
        )
        val base = createMinimalTestWGSPatientRecord()
        val record = base.copy(intolerances = listOf(intolerance), oncologicalHistory = treatments)
        val evaluation = function.evaluate(record)
        assertEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.warnGeneralMessages).containsExactly(
            "Immunotherapy related adverse events in history (Nivolumab induced pneumonitis)"
        )
    }

    @Test
    fun `Should evaluate to undetermined with prior immunotherapy treatment with unknown stop reason`() {
        val treatments = listOf(
            TreatmentTestFactory.treatmentHistoryEntry(
                treatments = setOf(treatment(TreatmentCategory.IMMUNOTHERAPY)), stopReason = null
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(withTreatmentHistory(treatments)))
    }

    private fun withTreatmentHistory(treatmentHistory: List<TreatmentHistoryEntry>): PatientRecord {
        val base = createMinimalTestWGSPatientRecord()
        return base.copy(oncologicalHistory = treatmentHistory)
    }

    private fun treatmentHistoryEntry(category: TreatmentCategory): TreatmentHistoryEntry {
        return TreatmentTestFactory.treatmentHistoryEntry(treatments = setOf(treatment(category)))
    }

    private fun treatment(category: TreatmentCategory): Treatment {
        return TreatmentTestFactory.drugTreatment("", category)
    }
}