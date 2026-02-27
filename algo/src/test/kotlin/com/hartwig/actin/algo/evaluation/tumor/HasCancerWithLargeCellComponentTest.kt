package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasCancerWithLargeCellComponentTest {

    private val function = HasCancerWithLargeCellComponent(TestDoidModelFactory.createMinimalTestDoidModel())

    @Test
    fun `Should evaluate to undetermined if no tumor doids configured`() {
        val tumorDetails = TumorTestFactory.withDoids(emptySet())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(tumorDetails))
    }

    @Test
    fun `Should pass if tumor has large cell component`() {
        val tumorDetails = TumorTestFactory.withDoids(setOf(DoidConstants.LARGE_CELL_CANCER_DOIDS.first()))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(tumorDetails))
    }

    @Test
    fun `Should be undetermined if tumor has neuroendocrine component`() {
        val tumorDetails = TumorTestFactory.withDoids(DoidConstants.NEUROENDOCRINE_DOIDS.first())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(tumorDetails))
    }

    @Test
    fun `Should fail if tumor is of other type than large cell`() {
        val tumorDetails = TumorTestFactory.withDoidAndName("wrong doid", "wrong name")
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(tumorDetails))
    }
}