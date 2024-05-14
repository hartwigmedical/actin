package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasCancerWithSmallCellComponentTest {

    val doidModel = TestDoidModelFactory.createWithOneDoidAndTerm(
        MATCHING_DOID,
        DoidConstants.SMALL_CELL_DOID_SET.iterator().next()
    )
    val function = HasCancerWithSmallCellComponent(doidModel)

    @Test
    fun `Should evaluate to undetermined if no tumor doids configured`() {
        val tumorDetails = TestTumorFactory.withDoids(emptySet())
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(tumorDetails))
    }

    @Test
    fun `Should pass if tumor is of small cell doid type`() {
        val tumorDetails = TestTumorFactory.withDoids(setOf(DoidConstants.SMALL_CELL_CARCINOMA_DOID))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(tumorDetails))
    }

    @Test
    fun `Should pass if small cell term in tumor type`() {
        val tumorDetails = TestTumorFactory.withDoidAndType(SOME_OTHER_DOID, "small cell lung cancer")
        assertEvaluation(EvaluationResult.PASS, function.evaluate(tumorDetails))
    }

    @Test
    fun `Should fail if non-small cell term in tumor type`() {
        val tumorDetails = TestTumorFactory.withDoidAndType(SOME_OTHER_DOID, "non-small cell lung cancer")
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(tumorDetails))
    }

    @Test
    fun `Should pass if small cell term in tumor extra details`() {
        val tumorDetails = TestTumorFactory.withDoidAndDetails(SOME_OTHER_DOID, "small cell")
        assertEvaluation(EvaluationResult.PASS, function.evaluate(tumorDetails))
    }

    @Test
    fun `Should fail if non-small cell term in tumor extra details`() {
        val tumorDetails = TestTumorFactory.withDoidAndType(SOME_OTHER_DOID, "non-small cell")
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(tumorDetails))
    }

    @Test
    fun `Should warn if tumor doid is neuroendocrine type but not small cell type`() {
        val tumorDetails = TestTumorFactory.withDoids(DoidConstants.NEUROENDOCRINE_CARCINOMA_DOID)
        assertEvaluation(EvaluationResult.WARN, function.evaluate(tumorDetails))
    }

    @Test
    fun `Should fail if tumor is of other type than small cell`() {
        val tumorDetails = TestTumorFactory.withDoidAndType("wrong doid", "wrong type")
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(tumorDetails))
    }

    companion object {
        const val MATCHING_DOID = "matching doid"
        const val SOME_OTHER_DOID = "some doid"
    }
}