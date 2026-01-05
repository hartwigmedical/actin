package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.doid.TestDoidModelFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HasTripleNegativeBreastCancerTest {

    val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
    val function = HasTripleNegativeBreastCancer(doidModel)

    @Test
    fun `Should evaluate to undetermined when no tumor doids configured`() {
        val evaluation = function.evaluate(TumorTestFactory.withDoids(emptySet()))
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings()).containsExactly("Undetermined if triple negative breast cancer (tumor doids missing)")
    }

    @Test
    fun `Should fail if tumor type is not breast cancer`() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids(setOf(DoidConstants.COLORECTAL_CANCER_DOID))))
    }

    @Test
    fun `Should pass if tumor doid is triple negative breast cancer`() {
        val patient = TumorTestFactory.withDoids(setOf(DoidConstants.BREAST_CANCER_DOID, DoidConstants.TRIPLE_NEGATIVE_BREAST_CANCER_DOID))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(patient))
    }

    @Test
    fun `Should pass if all breast cancer target receptors are negative with data source prior molecular tests`() {
        val patient = TumorTestFactory.withIhcTestsAndDoids(
            listOf(
                createIhcTest("PR", "Negative"),
                createIhcTest("ER", "Negative"),
                createIhcTest("HER2", "Negative")
            ), setOf(DoidConstants.BREAST_CANCER_DOID)
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(patient))
    }

    @Test
    fun `Should pass if all breast cancer target receptors are negative with data source scoreValue from prior molecular tests`() {
        val patient = TumorTestFactory.withIhcTestsAndDoids(
            listOf(
                createIhcTest(item = "PR", scoreValue = 0.0, scoreValueUnit = "%"),
                createIhcTest(item = "HER2", scoreValue = 1.0, scoreValueUnit = "+"),
                createIhcTest(item = "ER", scoreValue = 0.0, scoreValueUnit = "%")
            ), setOf(DoidConstants.BREAST_CANCER_DOID)
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(patient))
    }

    @Test
    fun `Should fail if if at least one of HER2 or PR or ER is positive`() {
        val patient =
            TumorTestFactory.withIhcTestsAndDoids(listOf(createIhcTest("HER2", "Positive")), setOf(DoidConstants.BREAST_CANCER_DOID))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(patient))
    }

    @Test
    fun `Should fail if HER2 molecular test result is positive with data source scoreValue`() {
        val patient = TumorTestFactory.withIhcTestsAndDoids(
            listOf(createIhcTest("HER2", scoreValue = 3.0, scoreValueUnit = "+")),
            setOf(DoidConstants.BREAST_CANCER_DOID)
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(patient))
    }

    @Test
    fun `Should evaluate to undetermined with specific message if prior molecular test data inconsistent`() {
        val evaluation = function.evaluate(
            TumorTestFactory.withIhcTestsAndDoids(
                listOf(createIhcTest("PR", "Negative"), createIhcTest("PR", "Positive")),
                setOf(DoidConstants.BREAST_CANCER_DOID)
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings()).containsExactly("Undetermined if triple negative breast cancer")
    }

    @Test
    fun `Should evaluate to undetermined if doids inconsistent`() {
        val evaluation = function.evaluate(
            TumorTestFactory.withIhcTestsAndDoids(
                emptyList(),
                setOf(
                    DoidConstants.BREAST_CANCER_DOID, DoidConstants.PROGESTERONE_POSITIVE_BREAST_CANCER_DOID,
                    DoidConstants.PROGESTERONE_NEGATIVE_BREAST_CANCER_DOID
                )
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings()).containsExactly("Undetermined if triple negative breast cancer")
    }

    @Test
    fun `Should evaluate to undetermined if prior molecular test data inconsistent with doids`() {
        val evaluation = function.evaluate(
            TumorTestFactory.withIhcTestsAndDoids(
                listOf(createIhcTest("PR", "Negative")),
                setOf(DoidConstants.BREAST_CANCER_DOID, DoidConstants.PROGESTERONE_POSITIVE_BREAST_CANCER_DOID)
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings()).containsExactly("Undetermined if triple negative breast cancer")
    }

    @Test
    fun `Should evaluate to undetermined if tumor doid is triple negative breast cancer but at least one of HER2 or PR or ER is positive`() {
        val evaluation = function.evaluate(
            TumorTestFactory.withIhcTestsAndDoids(
                listOf(createIhcTest("HER2", "Positive")),
                setOf(DoidConstants.BREAST_CANCER_DOID, DoidConstants.TRIPLE_NEGATIVE_BREAST_CANCER_DOID)
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings()).containsExactly("Undetermined if triple negative breast cancer")
    }

    @Test
    fun `Should evaluate to undetermined with specific message if HER2 IHC-score is 2+ and ER and PR molecular results are negative`() {
        val evaluation = function.evaluate(
            TumorTestFactory.withIhcTestsAndDoids(
                listOf(
                    createIhcTest("HER2", scoreValue = 2.0, scoreValueUnit = "+"),
                    createIhcTest(item = "PR", scoreValue = 0.0, scoreValueUnit = "%"),
                    createIhcTest(item = "ER", scoreValue = 0.0, scoreValueUnit = "%")
                ),
                setOf(DoidConstants.BREAST_CANCER_DOID)
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings()).containsExactly("Undetermined if triple negative breast cancer")
    }

    @Test
    fun `Should evaluate to undetermined if ER and PR and HER2 negative based on IHC but ERBB2 amp present`() {
        val evaluation = function.evaluate(
            TumorTestFactory.withDoidsAndAmplificationAndMolecularTest(
                setOf(DoidConstants.BREAST_CANCER_DOID), "ERBB2", listOf(
                    createIhcTest("HER2", "Negative"), createIhcTest("PR", "Negative"), createIhcTest("ER", "Negative")
                )
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings()).containsExactly("Undetermined if triple negative breast cancer (DOID/IHC data inconsistent with ERBB2 gene amp)")
    }

    @Test
    fun `Should evaluate to undetermined if ER and PR negative and no HER2 IHC present but ERBB2 amp present`() {
        val evaluation = function.evaluate(
            TumorTestFactory.withDoidsAndAmplificationAndMolecularTest(
                setOf(DoidConstants.BREAST_CANCER_DOID), "ERBB2", listOf(
                    createIhcTest("PR", "Negative"), createIhcTest("ER", "Negative")
                )
            )
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, evaluation)
        assertThat(evaluation.undeterminedMessagesStrings()).containsExactly("Undetermined if triple negative breast cancer (IHC HER2 data missing but ERBB2 amp so potentially not triple negative)")
    }

    private fun createIhcTest(
        item: String, scoreText: String = "Score", scoreValue: Double = 50.0, scoreValueUnit: String = "Unit"
    ) = IhcTest(
        item = item, scoreText = scoreText, scoreValue = scoreValue,
        scoreValueUnit = scoreValueUnit, impliesPotentialIndeterminateStatus = false
    )
}