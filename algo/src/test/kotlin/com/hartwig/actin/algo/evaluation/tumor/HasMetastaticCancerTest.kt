package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.tumor.HasMetastaticCancer.Companion.STAGE_II_POTENTIALLY_METASTATIC_CANCERS
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.TumorStage
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasMetastaticCancerTest {

    private val doidModel = TestDoidModelFactory.createWithOneParentChild("parent", "child")
    private val function = HasMetastaticCancer(doidModel)

    @Test
    fun `Should pass for (derived) tumor stage III`() {
        evaluateStage(TumorStage.III, EvaluationResult.PASS)
    }

    @Test
    fun `Should pass for (derived) tumor stage IV`() {
        evaluateStage(TumorStage.IV, EvaluationResult.PASS)
    }

    @Test
    fun `Should evaluate to undetermined if one of options among derived stage fails while others pass`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withTumorStageAndDerivedStages(null, setOf(TumorStage.III, TumorStage.II)))
        )
    }

    @Test
    fun `Should warn for (derived) tumor stage II in cancer type with possible metastatic disease in stage II`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                TumorTestFactory.withTumorStageAndDoid(
                    TumorStage.II,
                    STAGE_II_POTENTIALLY_METASTATIC_CANCERS.iterator().next()
                )
            )
        )

        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                TumorTestFactory.withTumorStageAndDerivedStagesAndDoid(
                    null,
                    setOf(TumorStage.II),
                    STAGE_II_POTENTIALLY_METASTATIC_CANCERS.iterator().next()
                )
            )
        )
    }

    @Test
    fun `Should fail for (derived) tumor stage I or II`() {
        evaluateStage(TumorStage.I, EvaluationResult.FAIL)
        evaluateStage(TumorStage.II, EvaluationResult.FAIL)
    }

    private fun evaluateStage(stage: TumorStage, expected: EvaluationResult) {
        assertEvaluation(expected, function.evaluate(TumorTestFactory.withTumorStage(stage)))
        assertEvaluation(expected, function.evaluate(TumorTestFactory.withTumorStageAndDerivedStages(null, setOf(stage))))
    }
}