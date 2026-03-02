package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.trial.input.datamodel.TumorTypeInput
import org.junit.jupiter.api.Test

class HasCancerOfUnknownPrimaryTest {

    private val tumorType = TumorTypeInput.ADENOCARCINOMA
    private val childDoid = "child"
    private val doidModel = TestDoidModelFactory.createWithOneParentChild(tumorType.doid(), childDoid)
    private val function = HasCancerOfUnknownPrimary(doidModel, tumorType)

    @Test
    fun `Should be undetermined if no doids configured`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withDoids(null)))
    }

    @Test
    fun `Should pass with correct tumor type and 'CUP' mentioned`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TumorTestFactory.withDoidAndName(
                    tumorType.doid(),
                    "Some ${TumorTermConstants.CUP_TERM}"
                )
            )
        )
    }

    @Test
    fun `Should warn if correct DOID assigned but 'CUP' not specifically mentioned`() {
        assertEvaluation(EvaluationResult.WARN, function.evaluate(TumorTestFactory.withDoids(childDoid)))
    }

    @Test
    fun `Should be undetermined if doid is exactly cancer`() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withDoids(DoidConstants.CANCER_DOID)))
    }

    @Test
    fun `Should be undetermined if doid is exactly cancer also if 'CUP' is mentioned`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TumorTestFactory.withDoidAndName(
                    DoidConstants.CANCER_DOID,
                    "Some ${TumorTermConstants.CUP_TERM}"
                )
            )
        )
    }

    @Test
    fun `Should fail with random doid`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids("random doid")))
    }
}