package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasHadSystemicTreatmentInAdvancedOrMetastaticSettingTest {

    private val function = HasHadSystemicTreatmentInAdvancedOrMetastaticSetting()

    @Test
    fun `Should pass if patient has had systemic treatment with palliative intent`() {
        val patientRecord = withTreatmentHistory(
            listOf(
                createTreatment(Intent.PALLIATIVE, systemic = true, "Treatment a"),
                createTreatment(Intent.PALLIATIVE, systemic = true, "Treatment b")
            )
        )
        val evaluation = function.evaluate(patientRecord)
        assertEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.passSpecificMessages)
            .containsExactly("Patient has had prior systemic treatment in advanced or metastatic setting (Treatment a and Treatment b)")
    }

    @Test
    fun `Should return undetermined if patient has had systemic treatment with unknown or induction, consolidation or maintenance intent`() {
        val unknownIntent = withTreatmentHistory(listOf(createTreatment(null, systemic = true)))
        val inductionIntent = withTreatmentHistory(listOf(createTreatment(Intent.INDUCTION, systemic = true)))
        val consolidationIntent = withTreatmentHistory(listOf(createTreatment(Intent.CONSOLIDATION, systemic = true)))
        val maintenanceIntent = withTreatmentHistory(listOf(createTreatment(Intent.MAINTENANCE, systemic = true)))

        listOf(unknownIntent, inductionIntent, consolidationIntent, maintenanceIntent).forEach {
            assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(it))
        }
    }

    @Test
    fun `Should fail for non-systemic treatment`() {
        val patientRecord = withTreatmentHistory(listOf(createTreatment(Intent.PALLIATIVE, systemic = false)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(patientRecord))
    }

    @Test
    fun `Should fail for (neo)adjuvant and curative treatments`() {
        val patientRecord = withTreatmentHistory(
            listOf(
                createTreatment(Intent.NEOADJUVANT, systemic = true),
                createTreatment(Intent.ADJUVANT, systemic = true),
                createTreatment(Intent.CURATIVE, systemic = true),
            )
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(patientRecord))
    }

    @Test
    fun `Should fail for empty treatment history`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(withTreatmentHistory(emptyList())))
    }

    private fun createTreatment(intent: Intent?, systemic: Boolean, name: String = "treatment name"): TreatmentHistoryEntry {
        return treatmentHistoryEntry(
            setOf(TreatmentTestFactory.treatment(name, isSystemic = systemic)),
            intents = intent?.let { setOf(it) }
        )
    }
}