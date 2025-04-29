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

class HasKnownCnsMetastasesTest {

    private val function = HasKnownCnsMetastases(simpleDoidModel)

    @Test
    fun `Should pass when CNS lesions present and no primary brain tumor`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TumorTestFactory.withBrainAndCnsLesionsAndDoids(
                    hasBrainLesions = false,
                    hasCnsLesions = true,
                    doids = setOf(OTHER_CHILD_CANCER_DOID)
                )
            )
        )
    }

    @Test
    fun `Should pass when brain lesions present and no primary brain tumor`() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TumorTestFactory.withBrainAndCnsLesionsAndDoids(
                    hasBrainLesions = true,
                    hasCnsLesions = false,
                    doids = setOf(OTHER_CHILD_CANCER_DOID)
                )
            )
        )
    }

    @Test
    fun `Should be undetermined when CNS lesions present and primary brain tumor`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TumorTestFactory.withBrainAndCnsLesionsAndDoids(
                    hasBrainLesions = false,
                    hasCnsLesions = true,
                    doids = setOf(REQUESTED_CHILD_CANCER_DOID)
                )
            )
        )
    }

    @Test
    fun `Should be undetermined when brain lesions present and primary brain tumor`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TumorTestFactory.withBrainAndCnsLesionsAndDoids(
                    hasBrainLesions = true,
                    hasCnsLesions = false,
                    doids = setOf(REQUESTED_CHILD_CANCER_DOID)
                )
            )
        )
    }

    @Test
    fun `Should warn when suspected CNS lesions present and no primary brain tumor`() {
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                TumorTestFactory.withBrainAndCnsLesionsAndDoids(
                    hasBrainLesions = false,
                    hasCnsLesions = false,
                    hasSuspectedCnsLesions = true,
                    doids = setOf(OTHER_CHILD_CANCER_DOID)
                )
            )
        )
    }

    @Test
    fun `Should be undetermined when suspected CNS lesions present and primary brain tumor`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TumorTestFactory.withBrainAndCnsLesionsAndDoids(
                    hasBrainLesions = false,
                    hasCnsLesions = false,
                    hasSuspectedCnsLesions = true,
                    doids = setOf(REQUESTED_CHILD_CANCER_DOID)
                )
            )
        )
    }

    @Test
    fun `Should return undetermined when brain lesion data is missing and primary brain tumor`() {
        val undetermined = function.evaluate(
            TumorTestFactory.withBrainAndCnsLesionsAndDoids(
                null,
                hasCnsLesions = null,
                doids = setOf(REQUESTED_CHILD_CANCER_DOID)
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, undetermined)
        assertThat(undetermined.undeterminedMessages).containsExactly("Has primary brain cancer hence undetermined if patient considers to have CNS metastases")
    }

    @Test
    fun `Should return undetermined when brain lesion data is missing and no primary brain tumor`() {
        val undetermined = function.evaluate(
            TumorTestFactory.withBrainAndCnsLesionsAndDoids(
                null,
                hasCnsLesions = null,
                doids = setOf(OTHER_CHILD_CANCER_DOID)
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, undetermined)
        assertThat(undetermined.undeterminedMessages).containsExactly("Undetermined if CNS metastases present (data missing)")
    }

    @Test
    fun `Should return undetermined when no CNS lesions but brain lesion data is missing`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withBrainAndCnsLesions(hasBrainLesions = null, hasCnsLesions = false))
        )
    }

    @Test
    fun `Should return undetermined when no brain lesions but CNS lesion data is missing`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withBrainAndCnsLesions(hasBrainLesions = false, hasCnsLesions = null))
        )
    }

    @Test
    fun `Should fail when neither CNS nor brain lesions are present`() {
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withBrainAndCnsLesions(hasBrainLesions = false, hasCnsLesions = false))
        )
    }

    companion object {
        private val simpleDoidModel = TestDoidModelFactory.createWithChildToParentMap(
            mapOf(REQUESTED_CHILD_CANCER_DOID to CANCER_DOID, OTHER_CHILD_CANCER_DOID to CANCER_DOID)
        )
    }
}