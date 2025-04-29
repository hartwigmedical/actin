package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.doid.TestDoidModelFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val CANCER_DOID = "100"
private const val REQUESTED_CHILD_CANCER_DOID = DoidConstants.BRAIN_CANCER_DOID
private const val OTHER_CHILD_CANCER_DOID = "300"

class HasKnownBrainMetastasesTest {

    private val function = HasKnownBrainMetastases(simpleDoidModel)

    @Test
    fun `Should pass when brain lesions present and no primary brain tumor`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withBrainLesionsAndDoids(true, doids = setOf(OTHER_CHILD_CANCER_DOID)))
        )
    }

    @Test
    fun `Should be undetermined when brain lesions present and primary brain tumor`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withBrainLesionsAndDoids(true, doids = setOf(REQUESTED_CHILD_CANCER_DOID)))
        )
    }

    @Test
    fun `Should warn when only suspected brain lesions present`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                TumorTestFactory.withBrainLesionsAndDoids(
                    hasBrainLesions = false,
                    hasSuspectedBrainLesions = true,
                    doids = setOf(OTHER_CHILD_CANCER_DOID)
                )
            )
        )
    }

    @Test
    fun `Should be undetermined when only suspected brain lesions present and primary brain tumor`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TumorTestFactory.withBrainLesionsAndDoids(
                    hasBrainLesions = false,
                    hasSuspectedBrainLesions = true,
                    doids = setOf(REQUESTED_CHILD_CANCER_DOID)
                )
            )
        )
    }

    @Test
    fun `Should return undetermined when brain lesion data is missing and primary brain tumor`() {
        val undetermined = function.evaluate(TumorTestFactory.withBrainLesionsAndDoids(null, doids = setOf(REQUESTED_CHILD_CANCER_DOID)))
        assertEvaluation(EvaluationResult.UNDETERMINED, undetermined)
        assertThat(undetermined.undeterminedMessages).containsExactly("Has primary brain cancer hence undetermined if patient considers to have brain metastases")
    }

    @Test
    fun `Should return undetermined when brain lesion data is missing`() {
        val undetermined = function.evaluate(TumorTestFactory.withBrainLesionsAndDoids(null))
        assertEvaluation(EvaluationResult.UNDETERMINED, undetermined)
        assertThat(undetermined.undeterminedMessages).containsExactly("Undetermined if brain metastases present (brain lesions data missing)")
    }

    @Test
    fun `Should fail when no brain lesions present`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withBrainLesionsAndDoids(false))
        )
    }

    companion object {
        private val simpleDoidModel = TestDoidModelFactory.createWithChildToParentMap(
            mapOf(REQUESTED_CHILD_CANCER_DOID to CANCER_DOID, OTHER_CHILD_CANCER_DOID to CANCER_DOID)
        )
    }
}