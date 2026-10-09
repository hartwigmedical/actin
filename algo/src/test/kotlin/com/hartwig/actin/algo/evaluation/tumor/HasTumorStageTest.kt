package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorStage
import org.junit.jupiter.api.Test

class HasTumorStageTest {

    private val function = HasTumorStage(setOf(TumorStage.IIIB))
    private val functionWithMultipleStages = HasTumorStage(setOf(TumorStage.IIIB, TumorStage.IVA))

    @Test
    fun `Should be undetermined if stage is null`() {
        evaluateFunctions(
            EvaluationResult.UNDETERMINED,
            TumorTestFactory.withTumorStage(null),
            "Exact tumor stage undetermined (tumor stage missing)",
            "Exact tumor stage undetermined (tumor stage missing)"
        )
    }

    @Test
    fun `Should pass for requested stage and display correct message`() {
        evaluateFunctions(
            EvaluationResult.PASS,
            TumorTestFactory.withTumorStage(TumorStage.IIIB),
            "Tumor stage IIIB meets requested stage(s) IIIB",
            "Tumor stage IIIB meets requested stage(s) IIIB or IVA"
        )
    }

    @Test
    fun `Should be undetermined if specific stage requested and category matches`() {
        evaluateFunctions(
            EvaluationResult.UNDETERMINED,
            TumorTestFactory.withTumorStage(TumorStage.III),
            "Undetermined if tumor stage III meets requested stage(s) IIIB",
            "Undetermined if tumor stage III meets requested stage(s) IIIB or IVA"
        )
    }

    @Test
    fun `Should fail for wrong stage`() {
        evaluateFunctions(
            EvaluationResult.FAIL,
            TumorTestFactory.withTumorStage(TumorStage.I),
            "Tumor stage I does not meet requested stage(s) IIIB",
            "Tumor stage I does not meet requested stage(s) IIIB or IVA"
        )
    }

    private fun evaluateFunctions(
        expected: EvaluationResult,
        record: PatientRecord,
        expectedMessage: String,
        expectedMessageForMultipleStages: String
    ) {
        assertEvaluation(expected, function.evaluate(record), expectedMessage)
        assertEvaluation(expected, functionWithMultipleStages.evaluate(record), expectedMessageForMultipleStages)
    }
}