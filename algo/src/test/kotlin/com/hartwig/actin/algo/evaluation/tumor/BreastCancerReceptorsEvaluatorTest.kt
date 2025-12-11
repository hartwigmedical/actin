package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.clinical.ReceptorType
import com.hartwig.actin.doid.TestDoidModelFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class BreastCancerReceptorsEvaluatorTest {

    val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
    val breastCancerReceptorsEvaluator = BreastCancerReceptorsEvaluator(doidModel)

    @Test
    fun `Should return NOT_BREAST_CANCER for colorectal cancer`() {
        assertThat(
            breastCancerReceptorsEvaluator.evaluate(
                setOf(DoidConstants.COLORECTAL_CANCER_DOID),
                emptyList(),
                ReceptorType.ER
            )
        ).isEqualTo(
            BreastCancerReceptorEvaluation.NOT_BREAST_CANCER
        )
    }

    @Test
    fun `Should return POSITIVE when evaluating ER for a breast cancer patient with positive ER IHC result`() {
        assertThat(
            breastCancerReceptorsEvaluator.evaluate(
                setOf(DoidConstants.BREAST_CANCER_DOID),
                listOf(IhcTest("ER", scoreText = "positive")),
                ReceptorType.ER
            )
        ).isEqualTo(
            BreastCancerReceptorEvaluation.POSITIVE
        )
    }

    @Test
    fun `Should return POSITIVE when evaluating ER for a patient with estrogen positive breast cancer DOID`() {
        assertThat(
            breastCancerReceptorsEvaluator.evaluate(
                setOf(
                    DoidConstants.ESTROGEN_POSITIVE_BREAST_CANCER_DOID,
                    DoidConstants.BREAST_CANCER_DOID
                ), emptyList(), ReceptorType.ER
            )
        ).isEqualTo(
            BreastCancerReceptorEvaluation.POSITIVE
        )
    }

    @Test
    fun `Should return NEGATIVE when evaluating ER for a breast cancer patient with negative ER IHC result`() {
        assertThat(
            breastCancerReceptorsEvaluator.evaluate(
                setOf(DoidConstants.BREAST_CANCER_DOID),
                listOf(IhcTest("ER", scoreText = "negative")),
                ReceptorType.ER
            )
        ).isEqualTo(
            BreastCancerReceptorEvaluation.NEGATIVE
        )
    }

    @Test
    fun `Should return NEGATIVE when evaluating ER for a patient with estrogen negative breast cancer DOID`() {
        assertThat(
            breastCancerReceptorsEvaluator.evaluate(
                setOf(
                    DoidConstants.ESTROGEN_NEGATIVE_BREAST_CANCER_DOID,
                    DoidConstants.BREAST_CANCER_DOID
                ), emptyList(), ReceptorType.ER
            )
        ).isEqualTo(
            BreastCancerReceptorEvaluation.NEGATIVE
        )
    }

    @Test
    fun `Should return NEGATIVE for a patient with triple negative breast cancer DOID`() {
        assertThat(
            breastCancerReceptorsEvaluator.evaluate(
                setOf(
                    DoidConstants.TRIPLE_NEGATIVE_BREAST_CANCER_DOID,
                    DoidConstants.BREAST_CANCER_DOID
                ), emptyList(), ReceptorType.ER
            )
        ).isEqualTo(
            BreastCancerReceptorEvaluation.NEGATIVE
        )
    }

    @Test
    fun `Should return BORDERLINE when evaluating HER2 for a breast cancer patient with 2+ HER2 result`() {
        assertThat(
            breastCancerReceptorsEvaluator.evaluate(
                setOf(DoidConstants.BREAST_CANCER_DOID),
                listOf(IhcTest("HER2", scoreValue = 2.0, scoreValueUnit = "+")),
                ReceptorType.HER2
            )
        ).isEqualTo(
            BreastCancerReceptorEvaluation.BORDERLINE
        )
    }

    @Test
    fun `Should return DATA_MISSING for breast cancer with no IHC result`() {
        assertThat(
            breastCancerReceptorsEvaluator.evaluate(
                setOf(DoidConstants.BREAST_CANCER_DOID),
                emptyList(),
                ReceptorType.HER2
            )
        ).isEqualTo(
            BreastCancerReceptorEvaluation.DATA_MISSING
        )
    }

    @Test
    fun `Should return INCONSISTENT_DATA when evaluating HER2 for a breast cancer patient with HER2 positive breast cancer DOID but negative IHC HER2 result`() {
        assertThat(
            breastCancerReceptorsEvaluator.evaluate(
                setOf(
                    DoidConstants.HER2_POSITIVE_BREAST_CANCER_DOID,
                    DoidConstants.BREAST_CANCER_DOID
                ), listOf(IhcTest("HER2", scoreText = "negative")), ReceptorType.HER2
            )
        ).isEqualTo(
            BreastCancerReceptorEvaluation.INCONSISTENT_DATA
        )
    }
}