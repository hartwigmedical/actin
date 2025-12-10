package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.molecular.IhcTestClassificationFunctions
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.clinical.ReceptorType
import com.hartwig.actin.doid.TestDoidModelFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class BreastCancerReceptorsEvaluatorTest {

    val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
    val breastCancerReceptorsEvaluator = BreastCancerReceptorsEvaluator(doidModel)

    @Test
    fun `Should return true for breast cancer`() {
        assertThat(breastCancerReceptorsEvaluator.isBreastCancer(setOf(DoidConstants.BREAST_CANCER_DOID))).isTrue()
    }

    @Test
    fun `Should return false for colorectal cancer`() {
        assertThat(breastCancerReceptorsEvaluator.isBreastCancer(setOf(DoidConstants.COLORECTAL_CANCER_DOID))).isFalse()
    }

    @Test
    fun `Should correctly summarize ER tests`() {
        val ihcTests = listOf(
            IhcTest(item = "ER", scoreText = "POSITIVE"),
            IhcTest(item = "ER", scoreValue = 20.0, scoreValueUnit = "%"),
            IhcTest(item = "HER2", scoreText = "NEGATIVE")
        )
        val summary = breastCancerReceptorsEvaluator.summarizeTests(ihcTests, ReceptorType.ER)
        assertThat(summary).contains(IhcTestClassificationFunctions.TestResult.NEGATIVE, IhcTestClassificationFunctions.TestResult.POSITIVE)
    }

    @Test
    fun `Should return true if only positive arguments`() {
        assertThat(breastCancerReceptorsEvaluator.receptorIsPositive(positiveArguments = true, negativeArguments = false)).isTrue()
    }

    @Test
    fun `Should return false if only negative arguments`() {
        assertThat(breastCancerReceptorsEvaluator.receptorIsPositive(positiveArguments = false, negativeArguments = true)).isFalse()
    }

    @Test
    fun `Should return null if both positive and negative arguments`() {
        assertThat(breastCancerReceptorsEvaluator.receptorIsPositive(positiveArguments = true, negativeArguments = true)).isNull()
    }

    @Test
    fun `Should return null if no positive or negative arguments`() {
        assertThat(breastCancerReceptorsEvaluator.receptorIsPositive(positiveArguments = false, negativeArguments = false)).isNull()
    }

    @Test
    fun `Should return true when ER positive based on positive test result`() {
        assertThat(
            breastCancerReceptorsEvaluator.positiveArguments(
                setOf(IhcTestClassificationFunctions.TestResult.POSITIVE),
                setOf(DoidConstants.BREAST_CANCER_DOID),
                ReceptorType.ER
            )
        ).isTrue()
    }

    @Test
    fun `Should return true when ER positive based on doid`() {
        assertThat(
            breastCancerReceptorsEvaluator.positiveArguments(
                emptySet(),
                setOf(DoidConstants.ESTROGEN_POSITIVE_BREAST_CANCER_DOID),
                ReceptorType.ER
            )
        ).isTrue()
    }

    @Test
    fun `Should return false when ER negative based on test result`() {
        assertThat(
            breastCancerReceptorsEvaluator.positiveArguments(
                setOf(IhcTestClassificationFunctions.TestResult.NEGATIVE),
                setOf(DoidConstants.BREAST_CANCER_DOID),
                ReceptorType.ER
            )
        ).isFalse()
    }

    @Test
    fun `Should return false when ER negative based on doid`() {
        assertThat(
            breastCancerReceptorsEvaluator.positiveArguments(
                emptySet(),
                setOf(DoidConstants.ESTROGEN_NEGATIVE_BREAST_CANCER_DOID),
                ReceptorType.ER
            )
        ).isFalse()
    }

    @Test
    fun `Should return false when ER positive based on positive test result`() {
        assertThat(
            breastCancerReceptorsEvaluator.negativeArguments(
                setOf(IhcTestClassificationFunctions.TestResult.POSITIVE),
                setOf(DoidConstants.BREAST_CANCER_DOID),
                ReceptorType.ER
            )
        ).isFalse()
    }

    @Test
    fun `Should return false when ER positive based on doid`() {
        assertThat(
            breastCancerReceptorsEvaluator.negativeArguments(
                emptySet(),
                setOf(DoidConstants.ESTROGEN_POSITIVE_BREAST_CANCER_DOID),
                ReceptorType.ER
            )
        ).isFalse()
    }

    @Test
    fun `Should return true when ER negative based on test result`() {
        assertThat(
            breastCancerReceptorsEvaluator.negativeArguments(
                setOf(IhcTestClassificationFunctions.TestResult.NEGATIVE),
                setOf(DoidConstants.BREAST_CANCER_DOID),
                ReceptorType.ER
            )
        ).isTrue()
    }

    @Test
    fun `Should return true when ER negative based on doid`() {
        assertThat(
            breastCancerReceptorsEvaluator.negativeArguments(
                emptySet(),
                setOf(DoidConstants.ESTROGEN_NEGATIVE_BREAST_CANCER_DOID),
                ReceptorType.ER
            )
        ).isTrue()
    }
}