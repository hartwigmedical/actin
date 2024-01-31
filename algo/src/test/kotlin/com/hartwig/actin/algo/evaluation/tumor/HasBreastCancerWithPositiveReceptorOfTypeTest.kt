package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasBreastCancerWithPositiveReceptorOfTypeTest {
    val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
    val function = HasBreastCancerWithPositiveReceptorOfType(doidModel, "PR")

    @Test
    fun `Should fail if tumor type not breast cancer`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                TumorTestFactory.withPriorMolecularTestsAndDoids(
                    listOf(createPriorMolecularTest("PR", "Positive")),
                    setOf(DoidConstants.COLORECTAL_CANCER_DOID)
                )
            )
        )
    }

    @Test
    fun `Should evaluate to undetermiend if no receptor info is present in doids or prior molecular tests`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(
                TumorTestFactory.withPriorMolecularTestsAndDoids(
                    emptyList(),
                    setOf(DoidConstants.BREAST_CANCER_DOID)
                )
            )
        )
    }

    @Test
    fun `Should evaluate to undetermiend if IHC data and DOIDS are inconsistent`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(
                TumorTestFactory.withPriorMolecularTestsAndDoids(
                    listOf(createPriorMolecularTest("PR", "Negative")),
                    setOf(DoidConstants.BREAST_CANCER_DOID, DoidConstants.PROGESTERONE_POSITIVE_BREAST_CANCER_DOID)
                )
            )
        )
    }

    @Test
    fun `Should pass if target receptor type is positive with data source doids`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, function.evaluate(
                TumorTestFactory.withPriorMolecularTestsAndDoids(
                    listOf(createPriorMolecularTest("Some test", "Negative")),
                    setOf(DoidConstants.BREAST_CANCER_DOID, DoidConstants.PROGESTERONE_POSITIVE_BREAST_CANCER_DOID)
                )
            )
        )
    }

    @Test
    fun `Should pass if target receptor type is positive with data source prior molecular tests`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, function.evaluate(
                TumorTestFactory.withPriorMolecularTestsAndDoids(
                    listOf(createPriorMolecularTest("PR", "Positive")),
                    setOf(DoidConstants.BREAST_CANCER_DOID)
                )
            )
        )
    }

    @Test
    fun `Should fail if target receptor type is negative`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                TumorTestFactory.withPriorMolecularTestsAndDoids(
                    listOf(createPriorMolecularTest("PR", "Negative")),
                    setOf(DoidConstants.BREAST_CANCER_DOID)
                )
            )
        )
    }

    companion object {
        private fun createPriorMolecularTest(item: String, score: String): PriorMolecularTest {
            return PriorMolecularTest(test = "IHC", item = item, scoreText = score, impliesPotentialIndeterminateStatus = false)
        }
    }
}