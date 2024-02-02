package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.clinical.datamodel.ReceptorType.HER2
import com.hartwig.actin.clinical.datamodel.ReceptorType.PR
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasBreastCancerWithPositiveReceptorOfTypeTest {
    val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
    val function = HasBreastCancerWithPositiveReceptorOfType(doidModel, PR)

    @Test
    fun `Should evaluate to undetermined when no tumor doids configured`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(
                TumorTestFactory.withPriorMolecularTestsAndDoids(
                    listOf(createPriorMolecularTest("PR", "Positive")), emptySet()
                )
            )
        )
    }

    @Test
    fun `Should fail if tumor type is not breast cancer`() {
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
    fun `Should evaluate to undetermined if no data is present for target receptor in doids or prior molecular tests`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(
                TumorTestFactory.withPriorMolecularTestsAndDoids(
                    listOf(createPriorMolecularTest("some test", "Positive"), createPriorMolecularTest("other test", "Positive")),
                    setOf(DoidConstants.BREAST_CANCER_DOID)
                )
            )
        )
    }

    @Test
    fun `Should evaluate to undetermined if prior molecular test data inconsistent`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(
                TumorTestFactory.withPriorMolecularTestsAndDoids(
                    listOf(createPriorMolecularTest("PR", "Negative"), createPriorMolecularTest("PR", "Positive")),
                    setOf(DoidConstants.BREAST_CANCER_DOID)
                )
            )
        )
    }

    @Test
    fun `Should evaluate to undetermined if doids inconsistent`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(
                TumorTestFactory.withPriorMolecularTestsAndDoids(
                    emptyList(),
                    setOf(
                        DoidConstants.BREAST_CANCER_DOID, DoidConstants.PROGESTERONE_POSITIVE_BREAST_CANCER_DOID,
                        DoidConstants.PROGESTERONE_NEGATIVE_BREAST_CANCER_DOID
                    )
                )
            )
        )
    }

    @Test
    fun `Should evaluate to undetermined if prior molecular test data inconsistent with doids`() {
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
                    listOf(createPriorMolecularTest("HER2", "Negative")),
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
    fun `Should pass if target receptor type is positive with data source scoreValue from prior molecular tests`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS, HasBreastCancerWithPositiveReceptorOfType(doidModel, HER2).evaluate(
                TumorTestFactory.withPriorMolecularTestsAndDoids(
                    listOf(createPriorMolecularTest(item = "HER2", scoreValue = 3.0, scoreValueUnit = "+")),
                    setOf(DoidConstants.BREAST_CANCER_DOID)
                )
            )
        )
    }

    @Test
    fun `Should fail if target receptor type is negative with data source doids`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                TumorTestFactory.withPriorMolecularTestsAndDoids(
                    emptyList(),
                    setOf(DoidConstants.BREAST_CANCER_DOID, DoidConstants.PROGESTERONE_NEGATIVE_BREAST_CANCER_DOID)
                )
            )
        )
    }

    @Test
    fun `Should fail if target receptor type is negative with data source scoreText from prior molecular tests`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                TumorTestFactory.withPriorMolecularTestsAndDoids(
                    listOf(createPriorMolecularTest("PR", "Negative")),
                    setOf(DoidConstants.BREAST_CANCER_DOID)
                )
            )
        )
    }

    @Test
    fun `Should fail if target receptor type is negative with data source scoreValue from prior molecular tests`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                TumorTestFactory.withPriorMolecularTestsAndDoids(
                    listOf(createPriorMolecularTest("PR", scoreValue = 0.0, scoreValueUnit = "%")),
                    setOf(DoidConstants.BREAST_CANCER_DOID)
                )
            )
        )
    }

    @Test
    fun `Should only use scoreValue from target receptor type in evaluation of prior molecular tests`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, function.evaluate(
                TumorTestFactory.withPriorMolecularTestsAndDoids(
                    listOf(
                        createPriorMolecularTest("HER2", scoreValue = 0.0, scoreValueUnit = "%"),
                        createPriorMolecularTest("PR", scoreValue = 80.0, scoreValueUnit = "%")
                    ),
                    setOf(DoidConstants.BREAST_CANCER_DOID)
                )
            )
        )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL, HasBreastCancerWithPositiveReceptorOfType(doidModel, HER2).evaluate(
                TumorTestFactory.withPriorMolecularTestsAndDoids(
                    listOf(
                        createPriorMolecularTest("HER2", scoreValue = 1.0, scoreValueUnit = "+"),
                        createPriorMolecularTest("PR", scoreValue = 3.0, scoreValueUnit = "%")
                    ),
                    setOf(DoidConstants.BREAST_CANCER_DOID)
                )
            )
        )
    }

    companion object {
        private fun createPriorMolecularTest(
            item: String, score: String = "Score", scoreValue: Double = 50.0, scoreValueUnit: String = "Unit"
        ): PriorMolecularTest {
            return PriorMolecularTest(
                test = "IHC", item = item, scoreText = score, scoreValue = scoreValue,
                scoreValueUnit = scoreValueUnit, impliesPotentialIndeterminateStatus = false
            )
        }
    }
}