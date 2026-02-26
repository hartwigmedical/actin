package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.jupiter.api.Test

class HasSolidPrimaryTumorTest {
    
    val function = HasSolidPrimaryTumor(createTestDoidModel())

    @Test
    fun shouldReturnUndeterminedForNullDoids() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withDoids(null)))
    }

    @Test
    fun shouldPassForCancerDoid() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withDoids(DoidConstants.CANCER_DOID)))
    }

    @Test
    fun shouldPassForBenignNeoplasmDoid() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withDoids(DoidConstants.BENIGN_NEOPLASM_DOID)))
    }

    @Test
    fun shouldWarnForWarnSolidCancerDoids() {
        val firstWarnDoid = HasSolidPrimaryTumor.WARN_SOLID_CANCER_DOIDS.first()
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(TumorTestFactory.withDoids(DoidConstants.CANCER_DOID, firstWarnDoid))
        )
    }

    @Test
    fun shouldFailForNonSolidCancerDoids() {
        val firstWarnDoid = HasSolidPrimaryTumor.WARN_SOLID_CANCER_DOIDS.first()
        val firstNonSolidDoid = DoidConstants.NON_SOLID_CANCER_DOIDS.first()
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withDoids(DoidConstants.CANCER_DOID, firstWarnDoid, firstNonSolidDoid))
        )
    }

    @Test
    fun shouldFailForNonCancerDoids() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids("arbitrary doid")))
    }

    companion object {
        private fun createTestDoidModel(): DoidModel {
            val childParentMap: Map<String, String> = listOf(
                DoidConstants.NON_SOLID_CANCER_DOIDS,
                HasSolidPrimaryTumor.WARN_SOLID_CANCER_DOIDS
            ).flatten().associateWith { DoidConstants.CANCER_DOID }

            return TestDoidModelFactory.createWithChildToParentMap(childParentMap)
        }
    }
}