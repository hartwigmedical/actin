package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasSolidPrimaryTumorTest {
    @Test
    fun canEvaluate() {
        val function = HasSolidPrimaryTumor(createTestDoidModel())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withDoids(null)))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withDoids(DoidConstants.CANCER_DOID)))
        val firstWarnDoid: String = HasSolidPrimaryTumor.WARN_SOLID_CANCER_DOIDS.iterator().next()
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(TumorTestFactory.withDoids(DoidConstants.CANCER_DOID, firstWarnDoid))
        )
        val firstNonSolidDoid: String = HasSolidPrimaryTumor.NON_SOLID_CANCER_DOIDS.iterator().next()
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(TumorTestFactory.withDoids(DoidConstants.CANCER_DOID, firstWarnDoid, firstNonSolidDoid))
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids("arbitrary doid")))
    }

    companion object {
        private fun createTestDoidModel(): DoidModel {
            val childParentMap: MutableMap<String, String> = mutableMapOf()
            for (nonSolidDoid in HasSolidPrimaryTumor.NON_SOLID_CANCER_DOIDS) {
                childParentMap[nonSolidDoid] = DoidConstants.CANCER_DOID
            }
            for (warnDoid in HasSolidPrimaryTumor.WARN_SOLID_CANCER_DOIDS) {
                childParentMap[warnDoid] = DoidConstants.CANCER_DOID
            }
            return TestDoidModelFactory.createWithChildToParentMap(childParentMap)
        }
    }
}