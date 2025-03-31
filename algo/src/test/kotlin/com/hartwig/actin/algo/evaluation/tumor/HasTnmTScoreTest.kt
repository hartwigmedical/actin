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
            EvaluationResult.UNDETERMINED, function(setOf(TnmT.T1), TumorTestFactory.withTumorStage(TumorStage.IV))
        )
    }

    @Test
    fun `Should pass if the score matches the tumor stage`(){
        assertEvaluation(
            EvaluationResult.PASS, function(setOf(TnmT.T4) , TumorTestFactory.withTumorStage(TumorStage.IIIA))
        )
    }

    @Test
    fun `Should fail if the score is not possible with the tumor stage`(){
        assertEvaluation(
            EvaluationResult.FAIL, function(setOf(TnmT.T2A) , TumorTestFactory.withTumorStage(TumorStage.IIA))
        )
    }
}

fun function(scores: Set<TnmT>, record: PatientRecord) = HasTnmTScore(scores).evaluate(record)