package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TumorStage
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasMetastaticCancerTest {
    @Test
    fun canEvaluate() {
        val matchDoid = "parent"
        val doidModel = TestDoidModelFactory.createWithOneParentChild(matchDoid, "child")
        val function = HasMetastaticCancer(doidModel)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withTumorStage(null)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withTumorStage(TumorStage.IV)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withTumorStage(TumorStage.IIIC)))
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                TumorTestFactory.withTumorStageAndDoid(
                    TumorStage.II,
                    HasMetastaticCancer.STAGE_II_POTENTIALLY_METASTATIC_CANCERS.iterator().next()
                )
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withTumorStageAndDoid(TumorStage.II, null)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withTumorStageAndDoid(TumorStage.II, "random")))
    }
}