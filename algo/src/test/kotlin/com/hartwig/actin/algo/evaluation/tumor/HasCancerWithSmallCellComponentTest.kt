package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasCancerWithSmallCellComponentTest {

    val doidModel = TestDoidModelFactory.createWithOneDoidAndTerm(
        "matching doid",
        HasCancerWithSmallCellComponent.SMALL_CELL_DOID_TERMS.iterator().next()
    )
    val function = HasCancerWithSmallCellComponent(doidModel)

    @Test
    fun `Should evaluate to undetermined if no tumor doids configured`() {
        val tumorDetails = TumorTestFactory.withDoids(emptySet())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(tumorDetails))
    }

    @Test
    fun `Should pass if tumor is of small cell doid type`() {
        val tumorDetails = TumorTestFactory.withDoids(setOf(DoidConstants.SMALL_CELL_CARCINOMA_DOID))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(tumorDetails))
    }

    @Test
    fun `Should pass if tumor is of small cell doid term`() {
        val tumorDetails = TumorTestFactory.withDoids(setOf("matching doid"))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(tumorDetails))
    }

    @Test
    fun `Should pass if small cell term in tumor type`() {
        val tumorDetails = TumorTestFactory.withDoidAndType("some doid", "small cell lung cancer")
        assertEvaluation(EvaluationResult.PASS, function.evaluate(tumorDetails))
    }

    @Test
    fun `Should pass if small cell term in tumor extra details`() {
        val tumorDetails = TumorTestFactory.withDoidAndDetails("some doid", "small cell")
        assertEvaluation(EvaluationResult.PASS, function.evaluate(tumorDetails))
    }

    @Test
    fun `Should warn if tumor doid is neuroendocrine type but not small cell type`() {
        val tumorDetails = TumorTestFactory.withDoids(DoidConstants.NEUROENDOCRINE_CARCINOMA_DOID)
        assertEvaluation(EvaluationResult.WARN, function.evaluate(tumorDetails))
    }

    @Test
    fun `Should fail if tumor is of other type than small cell`() {
        val tumorDetails = TumorTestFactory.withDoidAndType("wrong doid", "wrong type")
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(tumorDetails))
    }
}