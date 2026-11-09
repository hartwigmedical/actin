package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.datamodel.clinical.TumorStage
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.jupiter.api.Test

class HasOligoprogressiveDiseaseTest {

    private val function = HasOligoprogressiveDisease(TestDoidModelFactory.createMinimalTestDoidModel())

    @Test
    fun `Should fail when most recent treatment did not result in PD`() {
        val record = withHistory(entry(StopReason.TOXICITY, TreatmentResponse.PARTIAL_RESPONSE, 2023))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should fail when only an older treatment resulted in PD`() {
        val record = withHistory(
            entry(StopReason.PROGRESSIVE_DISEASE, TreatmentResponse.PROGRESSIVE_DISEASE, 2021),
            entry(StopReason.TOXICITY, TreatmentResponse.PARTIAL_RESPONSE, 2023)
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should evaluate to undetermined when most recent treatment resulted in PD`() {
        val record = withHistory(entry(StopReason.PROGRESSIVE_DISEASE, TreatmentResponse.PROGRESSIVE_DISEASE, 2023))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record))
    }

    @Test
    fun `Should evaluate to undetermined when most recent line is unclear and one of the candidates resulted in PD`() {
        val record = withHistory(
            entry(StopReason.PROGRESSIVE_DISEASE, TreatmentResponse.PROGRESSIVE_DISEASE, 2023),
            entry(StopReason.TOXICITY, TreatmentResponse.PARTIAL_RESPONSE, null)
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record))
    }

    @Test
    fun `Should fail when most recent line is unclear but no candidate resulted in PD`() {
        val record = withHistory(
            entry(StopReason.TOXICITY, TreatmentResponse.PARTIAL_RESPONSE, 2023),
            entry(StopReason.TOXICITY, TreatmentResponse.PARTIAL_RESPONSE, null)
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should fail without prior systemic treatment`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withTumorStage(TumorStage.IV)))
    }

    @Test
    fun `Should fail when only non-systemic treatment resulted in PD`() {
        val record = withHistory(entry(StopReason.PROGRESSIVE_DISEASE, TreatmentResponse.PROGRESSIVE_DISEASE, 2023, isSystemic = false))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should fail for non-metastatic stage even when most recent treatment resulted in PD`() {
        val record = withHistory(entry(StopReason.PROGRESSIVE_DISEASE, TreatmentResponse.PROGRESSIVE_DISEASE, 2023))
            .copy(tumor = TumorDetails(stage = TumorStage.I))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should evaluate to undetermined when tumor stage is missing and most recent treatment resulted in PD`() {
        val record = withHistory(entry(StopReason.PROGRESSIVE_DISEASE, TreatmentResponse.PROGRESSIVE_DISEASE, 2023))
            .copy(tumor = TumorDetails(stage = null))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record))
    }

    private fun entry(stopReason: StopReason, response: TreatmentResponse, startYear: Int?, isSystemic: Boolean = true): TreatmentHistoryEntry {
        return TreatmentTestFactory.treatmentHistoryEntry(
            setOf(TreatmentTestFactory.treatment("1", isSystemic)),
            startYear = startYear,
            stopReason = stopReason,
            bestResponse = response
        )
    }

    private fun withHistory(vararg entries: TreatmentHistoryEntry): PatientRecord {
        return TreatmentTestFactory.withTreatmentHistory(entries.toList())
    }
}
