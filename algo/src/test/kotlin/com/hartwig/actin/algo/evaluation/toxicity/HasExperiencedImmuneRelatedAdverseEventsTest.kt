package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.TestPatientFactory.createMinimalTestWGSPatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.icd.TestIcdFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val IMMUNOTHERAPY_TOX_ENTRY = TreatmentTestFactory.treatmentHistoryEntry(
    treatments = setOf(TreatmentTestFactory.drugTreatment("immunoName", TreatmentCategory.IMMUNOTHERAPY)), stopReason = StopReason.TOXICITY
)
private val IMMUNOTHERAPY_PD_ENTRY =
    IMMUNOTHERAPY_TOX_ENTRY.copy(treatmentHistoryDetails = TreatmentHistoryDetails(stopReason = StopReason.PROGRESSIVE_DISEASE))
private val IMMUNO_INTOLERANCE = Intolerance(
    name = "Nivolumab induced pneumonitis",
    icdCode = IcdCode(IcdConstants.DRUG_ALLERGY_CODE, IcdConstants.IMMUNOTHERAPY_DRUG_SET.first())
)

class HasExperiencedImmuneRelatedAdverseEventsTest {
    private val function = HasExperiencedImmuneRelatedAdverseEvents(TestIcdFactory.createTestModel())

    @Test
    fun `Should fail with no treatmentHistory`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should fail with no immunotherapy in history`() {
        val record = createMinimalTestWGSPatientRecord().copy(
            intolerances = listOf(IMMUNO_INTOLERANCE),
            oncologicalHistory = emptyList()
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should warn with prior immunotherapy treatment and stop reason toxicity`() {
        val treatments = listOf(IMMUNOTHERAPY_TOX_ENTRY)
        val evaluation = function.evaluate(TreatmentTestFactory.withTreatmentHistory(treatments))
        assertEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.warnGeneralMessages).containsExactly(
            "Possible immunotherapy related adverse events in history (prior immunotherapy with stop reason toxicity)"
        )
    }

    @Test
    fun `Should warn for prior immunotherapy treatment and immunotherapy intolerance in history`() {
        val record = createMinimalTestWGSPatientRecord().copy(
            intolerances = listOf(IMMUNO_INTOLERANCE),
            oncologicalHistory = listOf(IMMUNOTHERAPY_PD_ENTRY)
        )
        val evaluation = function.evaluate(record)
        assertEvaluation(EvaluationResult.WARN, evaluation)
        assertThat(evaluation.warnGeneralMessages).containsExactly(
            "Possible immunotherapy related adverse events in history (Nivolumab induced pneumonitis)"
        )
    }

    @Test
    fun `Should evaluate to undetermined for prior immunotherapy treatment and drug intolerance in history with unknown extension code`() {
        val record = createMinimalTestWGSPatientRecord().copy(
            intolerances = listOf(IMMUNO_INTOLERANCE.copy(icdCode = IcdCode(IcdConstants.DRUG_ALLERGY_CODE, null))),
            oncologicalHistory = listOf(IMMUNOTHERAPY_PD_ENTRY)
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record))
    }

    @Test
    fun `Should evaluate to undetermined with prior immunotherapy treatment with unknown stop reason`() {
        val treatment = IMMUNOTHERAPY_TOX_ENTRY.copy(treatmentHistoryDetails = TreatmentHistoryDetails(stopReason = null))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withTreatmentHistory(listOf(treatment))))
    }
}