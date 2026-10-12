package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorStage
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.jupiter.api.Test

class HasMetastaticCancerTest {

    private val doidModel = TestDoidModelFactory.createWithOneParentChild("parent", "child")
    private val function = HasMetastaticCancer(doidModel)

    @Test
    fun `Should pass for stage IV`() {
        assertEvaluation(EvaluationResult.PASS, evaluateFunction(TumorStage.IV), "Stage IV is considered metastatic")
    }


    @Test
    fun `Should be undetermined for stage III`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            evaluateFunction(TumorStage.III),
            "Undetermined if stage III is considered metastatic"
        )
    }

    @Test
    fun `Should be undetermined for tumor stage II in cancer type with possible metastatic disease in stage II`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TumorTestFactory.withTumorStageAndDoid(
                    TumorStage.II,
                    MetastaticCancerEvaluator.STAGE_II_POTENTIALLY_METASTATIC_CANCER_DOIDS.first()
                )
            ),
            "Undetermined if stage II is considered metastatic"
        )
    }

    @Test
    fun `Should fail for tumor stage I or II`() {
        assertEvaluation(EvaluationResult.FAIL, evaluateFunction(TumorStage.I), "Stage I is not considered metastatic")
        assertEvaluation(EvaluationResult.FAIL, evaluateFunction(TumorStage.II), "Stage II is not considered metastatic")
    }

    @Test
    fun `Should be undetermined when no tumor stage provided`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluateFunction(null), "Undetermined if metastatic cancer (tumor stage missing)")
    }

    private fun evaluateFunction(stage: TumorStage?): Evaluation {
        return function.evaluate(TumorTestFactory.withTumorStage(stage))
    }
}