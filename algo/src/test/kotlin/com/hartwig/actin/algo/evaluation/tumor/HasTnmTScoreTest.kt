package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TnmT
import com.hartwig.actin.datamodel.clinical.TumorStage
import org.junit.Test

class HasTnmTScoreTest {
    @Test
    fun `Should be undetermined if the tumor is TNM M`(){
        assertEvaluation(
            EvaluationResult.UNDETERMINED, function(setOf(TnmT.T1), TumorTestFactory.withTumorStage(TumorStage.IVA))
        )
    }

    @Test
    fun `Should pass if the score matches the tumor stage`(){
        assertEvaluation(
            EvaluationResult.PASS, function(setOf(TnmT.T2A, TnmT.T2), TumorTestFactory.withTumorStage(TumorStage.IB))
        )
    }

    @Test
    fun `Should fail if the score is not possible with the tumor stage`(){
        assertEvaluation(
            EvaluationResult.FAIL, function(setOf(TnmT.T2A) , TumorTestFactory.withTumorStage(TumorStage.IIA))
        )
    }

    @Test
    fun `Should pass if the targets contains all possible TnmTs of the patient`() {
        assertEvaluation(
            EvaluationResult.PASS, function(setOf(TnmT.T2, TnmT.T4, TnmT.T2A) , TumorTestFactory.withTumorStage(TumorStage.IB))
        )
    }

    @Test
    fun `Should be undetermined if only some of the possible TnmTs exist in the target set`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED, function(setOf(TnmT.T1A, TnmT.T4, TnmT.T2B) , TumorTestFactory.withTumorStage(TumorStage.IIB))
        )
    }

    @Test
    fun `Should use derived stages if stage is null`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function(setOf(TnmT.T1A, TnmT.T4, TnmT.T2B), TumorTestFactory.withTumorStageAndDerivedStages(null, setOf(TumorStage.IIB)))
        )
    }

    @Test
    fun `Should pass if the target TnmTs list all possible TnmTs of the derived stage`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function(setOf(TnmT.T1, TnmT.T1A, TnmT.T1B, TnmT.T1C, TnmT.T4, TnmT.T2B), TumorTestFactory.withTumorStageAndDerivedStages(null, setOf(TumorStage.IIB, TumorStage.IA)))
        )
    }

    @Test
    fun `Should be undetermined if both the tumor stage and the derived tumor stages are not present`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function(setOf(TnmT.T1, TnmT.T1A, TnmT.T1B, TnmT.T1C, TnmT.T4, TnmT.T2B), TumorTestFactory.withTumorStageAndDerivedStages(null, null))
        )
    }
}

private fun function(scores: Set<TnmT>, record: PatientRecord) = HasTnmTScore(scores).evaluate(record)