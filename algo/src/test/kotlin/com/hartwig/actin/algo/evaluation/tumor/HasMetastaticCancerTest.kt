package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.tumor.HasMetastaticCancer.Companion.STAGE_II_POTENTIALLY_METASTATIC_CANCER_DOIDS
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorStage
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasMetastaticCancerTest {

    private val doidModel = TestDoidModelFactory.createWithOneParentChild("parent", "child")
    private val function = HasMetastaticCancer(doidModel)

    @Test
    fun `Should pass for stage III or IV`() {
        assertEvaluation(EvaluationResult.PASS, evaluateFunction(TumorStage.III))
        assertEvaluation(EvaluationResult.PASS, evaluateFunction(TumorStage.IV))
    }

    @Test
    fun `Should be undetermined for tumor stage II in cancer type with possible metastatic disease in stage II`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TumorTestFactory.withTumorStageAndDoid(
                    TumorStage.II,
                    STAGE_II_POTENTIALLY_METASTATIC_CANCER_DOIDS.first()
                )
            )
        )
    }

    @Test
    fun `Should fail for tumor stage I or II`() {
        assertEvaluation(EvaluationResult.FAIL, evaluateFunction(TumorStage.I))
        assertEvaluation(EvaluationResult.FAIL, evaluateFunction(TumorStage.II))
    }

    @Test
    fun `Should fail cancer type with possible metastatic disease in stage II and stage I`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                TumorTestFactory.withTumorStageAndDoid(
                    TumorStage.I,
                    STAGE_II_POTENTIALLY_METASTATIC_CANCER_DOIDS.iterator().next()
                )
            )
        )
    }

    @Test
    fun `Should be undetermined when no tumor stage provided`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluateFunction(null))
    }

    private fun evaluateFunction(stage: TumorStage?): Evaluation {
        return function.evaluate(TumorTestFactory.withTumorStage(stage))
    }
}