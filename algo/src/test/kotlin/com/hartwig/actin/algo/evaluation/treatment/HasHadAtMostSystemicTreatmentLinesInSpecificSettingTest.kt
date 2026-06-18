package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.withTreatmentHistory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import org.junit.jupiter.api.Test

class HasHadAtMostSystemicTreatmentLinesInSpecificSettingTest {

    private val function = HasHadAtMostSystemicTreatmentLinesInSpecificSetting(
        intentsToIgnore = Intent.curativeAdjuvantNeoadjuvantSet(),
        settingDescription = "metastatic",
        maximumLines = 2
    )
    private val treatment = createTreatment(intent = null)

    @Test
    fun `Should pass with no prior systemic treatment`() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(withTreatmentHistory(emptyList())))
    }

    @Test
    fun `Should pass with only excluded intent treatments`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(withTreatmentHistory(listOf(createTreatment(Intent.CURATIVE))))
        )
    }

    @Test
    fun `Should fail if palliative lines alone exceed maximum`() {
        val record = withTreatmentHistory(
            listOf(Intent.PALLIATIVE, Intent.PALLIATIVE, Intent.PALLIATIVE).map { treatment.copy(intents = setOf(it)) }
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should fail when total lines exceed maximum plus one uncertain buffer`() {
        val record = withTreatmentHistory(
            listOf(Intent.PALLIATIVE, Intent.MAINTENANCE, Intent.INDUCTION, null).map { treatment.copy(intents = it?.let { setOf(it) }) }
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should be undetermined when uncertain lines could push total over maximum`() {
        val record = withTreatmentHistory(
            listOf(Intent.PALLIATIVE, Intent.MAINTENANCE, null).map { treatment.copy(intents = it?.let { setOf(it) }) }
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record))
    }

    @Test
    fun `Should pass when total included lines are at the maximum`() {
        val record = withTreatmentHistory(
            listOf(Intent.PALLIATIVE, Intent.MAINTENANCE).map { treatment.copy(intents = setOf(it)) }
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should pass when included lines are below the maximum`() {
        val record = withTreatmentHistory(listOf(treatment.copy(intents = setOf(Intent.PALLIATIVE))))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    private fun createTreatment(intent: Intent?): TreatmentHistoryEntry {
        return treatmentHistoryEntry(
            setOf(TreatmentTestFactory.treatment("treatment", isSystemic = true)),
            intents = intent?.let { setOf(it) }
        )
    }
}
