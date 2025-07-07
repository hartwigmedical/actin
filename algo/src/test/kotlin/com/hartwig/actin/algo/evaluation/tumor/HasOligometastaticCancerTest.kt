package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorStage
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasOligometastaticCancerTest {

    private val doidModel = TestDoidModelFactory.createWithOneParentChild("parent", "child")
    private val function = HasOligometastaticCancer(doidModel)

    @Test
    fun `Should return undetermined for stage III or IV`() {
        listOf(TumorStage.III, TumorStage.IV).forEach { stage -> assertEvaluation(EvaluationResult.UNDETERMINED, evaluateFunction(stage)) }
    }

    @Test
    fun `Should return undetermined for tumor stage II in cancer type with possible metastatic disease in stage II`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TumorTestFactory.withTumorStageAndDoid(
                    TumorStage.II,
                    MetastaticCancerEvaluator.STAGE_II_POTENTIALLY_METASTATIC_CANCER_DOIDS.first()
                )
            )
        )
    }

    @Test
    fun `Should fail for tumor stage I or II`() {
        listOf(TumorStage.I, TumorStage.II).forEach { stage -> assertEvaluation(EvaluationResult.FAIL, evaluateFunction(stage)) }
    }

    @Test
    fun `Should return undetermined when no tumor stage provided`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluateFunction(null))
    }

    private fun evaluateFunction(stage: TumorStage?): Evaluation {
        return function.evaluate(TumorTestFactory.withTumorStage(stage))
    }
}