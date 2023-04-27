package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasSolidPrimaryTumorIncludingLymphomaTest {
    @Test
    fun canEvaluate() {
        val function = HasSolidPrimaryTumorIncludingLymphoma(createTestDoidModel())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withDoids(null)))
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withDoids(DoidConstants.CANCER_DOID))
        )
        val firstWarnDoid: String = HasSolidPrimaryTumorIncludingLymphoma.WARN_SOLID_CANCER_DOIDS.iterator().next()
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(TumorTestFactory.withDoids(DoidConstants.CANCER_DOID, firstWarnDoid))
        )
        val firstNonSolidDoid: String = HasSolidPrimaryTumorIncludingLymphoma.NON_SOLID_CANCER_DOIDS.iterator().next()
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                TumorTestFactory.withDoids(
                    DoidConstants.CANCER_DOID,
                    firstWarnDoid,
                    firstNonSolidDoid
                )
            )
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids("arbitrary doid")))
    }

    companion object {
        private fun createTestDoidModel(): DoidModel {
            val childParentMap: MutableMap<String, String> = mutableMapOf()
            for (nonSolidDoid in HasSolidPrimaryTumorIncludingLymphoma.NON_SOLID_CANCER_DOIDS) {
                childParentMap[nonSolidDoid] = DoidConstants.CANCER_DOID
            }
            for (warnDoid in HasSolidPrimaryTumorIncludingLymphoma.WARN_SOLID_CANCER_DOIDS) {
                childParentMap[warnDoid] = DoidConstants.CANCER_DOID
            }
            return TestDoidModelFactory.createWithChildToParentMap(childParentMap)
        }
    }
}