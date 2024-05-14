package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasSolidPrimaryTumorTest {
    val function = HasSolidPrimaryTumor(createTestDoidModel())

    @Test
    fun shouldReturnUndeterminedForNullDoids() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestTumorFactory.withDoids(null)))
    }

    @Test
    fun shouldPassForCancerDoid() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TestTumorFactory.withDoids(DoidConstants.CANCER_DOID)))
    }

    @Test
    fun shouldPassForBenignNeoplasmDoid() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TestTumorFactory.withDoids(DoidConstants.BENIGN_NEOPLASM_DOID)))
    }

    @Test
    fun shouldWarnForWarnSolidCancerDoids() {
        val firstWarnDoid: String = HasSolidPrimaryTumor.WARN_SOLID_CANCER_DOIDS.iterator().next()
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(TestTumorFactory.withDoids(DoidConstants.CANCER_DOID, firstWarnDoid))
        )
    }

    @Test
    fun shouldFailForNonSolidCancerDoids() {
        val firstWarnDoid: String = HasSolidPrimaryTumor.WARN_SOLID_CANCER_DOIDS.iterator().next()
        val firstNonSolidDoid: String = HasSolidPrimaryTumor.NON_SOLID_CANCER_DOIDS.iterator().next()
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TestTumorFactory.withDoids(DoidConstants.CANCER_DOID, firstWarnDoid, firstNonSolidDoid))
        )
    }

    @Test
    fun shouldFailForNonCancerDoids() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestTumorFactory.withDoids("arbitrary doid")))
    }

    companion object {
        private fun createTestDoidModel(): DoidModel {
            val childParentMap: Map<String, String> = listOf(
                HasSolidPrimaryTumor.NON_SOLID_CANCER_DOIDS,
                HasSolidPrimaryTumor.WARN_SOLID_CANCER_DOIDS
            ).flatten().associateWith { DoidConstants.CANCER_DOID }

            return TestDoidModelFactory.createWithChildToParentMap(childParentMap)
        }
    }
}