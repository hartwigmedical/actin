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
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withDoids(null)),
            "Solid primary tumor undetermined (tumor type missing)"
        )
    }

    @Test
    fun shouldPassForCancerDoid() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withDoids(DoidConstants.CANCER_DOID)),
            "Primary tumor is solid"
        )
    }

    @Test
    fun shouldPassForBenignNeoplasmDoid() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withDoids(DoidConstants.BENIGN_NEOPLASM_DOID)),
            "Primary tumor is solid"
        )
    }

    @Test
    fun shouldWarnForWarnSolidCancerDoids() {
        val firstWarnDoid = HasSolidPrimaryTumor.WARN_SOLID_CANCER_DOIDS.first()
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(TumorTestFactory.withDoids(DoidConstants.CANCER_DOID, firstWarnDoid)),
            "Unclear if primary tumor is considered solid"
        )
    }

    @Test
    fun shouldFailForNonSolidCancerDoids() {
        val firstWarnDoid = HasSolidPrimaryTumor.WARN_SOLID_CANCER_DOIDS.first()
        val firstNonSolidDoid = DoidConstants.NON_SOLID_CANCER_DOIDS.first()
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withDoids(DoidConstants.CANCER_DOID, firstWarnDoid, firstNonSolidDoid)),
            "No solid primary tumor"
        )
    }

    @Test
    fun shouldFailForNonCancerDoids() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids("arbitrary doid")), "No solid primary tumor")
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