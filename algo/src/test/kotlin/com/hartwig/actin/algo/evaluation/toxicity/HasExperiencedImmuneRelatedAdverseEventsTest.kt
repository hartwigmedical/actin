package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestPatientFactory.createMinimalTestWGSPatientRecord
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.Intolerance
import com.hartwig.actin.clinical.datamodel.TreatmentTestFactory
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
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
        assertEvaluation(EvaluationResult.WARN, function.evaluate(withTreatmentHistory(treatments)))
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
            "Nivolumab", setOf(DoidConstants.DRUG_ALLERGY_DOID), "", setOf(""), "", "", "", "", setOf(TreatmentCategory.IMMUNOTHERAPY)
        )
        val base = createMinimalTestWGSPatientRecord()
        val record = base.copy(intolerances = listOf(intolerance), oncologicalHistory = treatments)
        assertEvaluation(EvaluationResult.WARN, function.evaluate(record))
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