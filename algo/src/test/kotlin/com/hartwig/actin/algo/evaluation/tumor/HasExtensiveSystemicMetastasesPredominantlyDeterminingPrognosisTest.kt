package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TumorStage
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasExtensiveSystemicMetastasesPredominantlyDeterminingPrognosisTest {

    private val doidModel = TestDoidModelFactory.createWithOneParentChild("parent", "child")
    private val function = HasExtensiveSystemicMetastasesPredominantlyDeterminingPrognosis(doidModel)

    @Test
    fun `Should fail when no metastatic cancer`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withTumorStageAndDoid(TumorStage.II, "random")))
    }

    @Test
    fun `Should be undetermined when tumor stage unknown`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withTumorStage(null)))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withTumorStageAndDoid(TumorStage.II, null)))
    }

    @Test
    fun `Should be undetermined when patient has metastatic cancer`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withTumorStage(TumorStage.IV)))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withTumorStage(TumorStage.IIIC)))
    }
}