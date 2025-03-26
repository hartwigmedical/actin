package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorStage
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasTnmTScoreTest {
    @Test
    fun `Should PASS if the tumor and request are both TNM M`(){
        assertEvaluation(
            EvaluationResult.PASS, function("M", TumorTestFactory.withTumorStage(TumorStage.IV))
        )
    }
}

fun function(score: String, record: PatientRecord) = HasTnmTScore(score).evaluate(record)