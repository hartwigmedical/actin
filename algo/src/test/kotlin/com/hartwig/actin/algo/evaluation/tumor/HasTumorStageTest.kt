package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorStage
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasTumorStageTest {

    private val function = HasTumorStage(setOf(TumorStage.IIIB))
    private val functionWithMultipleStages = HasTumorStage(setOf(TumorStage.IIIB, TumorStage.IVA))

    @Test
    fun `Should be undetermined if stage is null`() {
        evaluateFunctions(EvaluationResult.UNDETERMINED, TumorTestFactory.withTumorStage(null))
    }

    @Test
    fun `Should pass for requested stage and display correct message`() {
        evaluateFunctions(EvaluationResult.PASS, TumorTestFactory.withTumorStage(TumorStage.IIIB))
        assertThat(
            functionWithMultipleStages.evaluate(TumorTestFactory.withTumorStage(TumorStage.IIIB)).passMessagesStrings()
        ).containsExactly("Tumor stage IIIB meets requested stage(s) IIIB or IVA")
    }

    @Test
    fun `Should be undetermined if specific stage requested and category matches`() {
        evaluateFunctions(EvaluationResult.UNDETERMINED, TumorTestFactory.withTumorStage(TumorStage.III))
    }

    @Test
    fun `Should fail for wrong stage`() {
        evaluateFunctions(EvaluationResult.FAIL, TumorTestFactory.withTumorStage(TumorStage.I))
    }

    private fun evaluateFunctions(expected: EvaluationResult, record: PatientRecord) {
        assertEvaluation(expected, function.evaluate(record))
        assertEvaluation(expected, functionWithMultipleStages.evaluate(record))
    }
}