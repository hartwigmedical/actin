package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Assert
import org.junit.Test

class HasBreastCancerHormonePositiveHER2NegativeTest {
    val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
    val function = HasBreastCancerHormonePositiveHER2Negative(doidModel)

    @Test
    fun `Should evaluate to undetermined if no doids configured`() {
        assertEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(
                TumorTestFactory.withPriorMolecularTestsAndDoids(
                    listOf(createPriorMolecularTest("ER", "Positive")), null
                )
            )
        )
    }

    @Test
    fun `Should warn when hormone positive and HER2 negative with ERBB2 amplification`() {
        assertEvaluation(
            EvaluationResult.WARN, function.evaluate(
                TumorTestFactory.withDoidsAndAmplification(
                    setOf(
                        DoidConstants.BREAST_CANCER_DOID,
                        DoidConstants.HER2_NEGATIVE_BREAST_CANCER_DOID,
                        DoidConstants.ESTROGEN_POSITIVE_BREAST_CANCER_DOID
                    ), "ERBB2"
                )
            )
        )
    }

    @Test
    fun `Should pass when certainly hormone-positive HER2-negative `() {
        assertEvaluation(
            EvaluationResult.PASS, function.evaluate(
                TumorTestFactory.withPriorMolecularTestsAndDoids(
                    listOf(createPriorMolecularTest("HER2", "Negative"), createPriorMolecularTest("PR", "Positive")),
                    setOf(
                        DoidConstants.BREAST_CANCER_DOID
                    )
                )
            )
        )
    }

    @Test
    fun `Should evaluate to undetermined when certainly HER2-negative with unclear HR-status`() {
        Assert.assertEquals(
            setOf("Undetermined HR+ HER2- breast cancer due to HR status"), function.evaluate(
                TumorTestFactory.withPriorMolecularTestsAndDoids(
                    listOf(createPriorMolecularTest("HER2", "Negative")),
                    setOf(
                        DoidConstants.BREAST_CANCER_DOID
                    )
                )
            ).undeterminedGeneralMessages
        )
    }

    @Test
    fun `Should evaluate to undetermined when certainly hormone-positive with unclear HER2-status`() {
        Assert.assertEquals(
            setOf("Undetermined HR+ HER2- breast cancer due to HER2 status"), function.evaluate(
                TumorTestFactory.withPriorMolecularTestsAndDoids(
                    listOf(createPriorMolecularTest("ER", "Positive"), createPriorMolecularTest("PR", "Positive")),
                    setOf(
                        DoidConstants.BREAST_CANCER_DOID
                    )
                )
            ).undeterminedGeneralMessages
        )
    }

    @Test
    fun `Should fail with certain HR- or HER2+ status`() {
        Assert.assertEquals(
            setOf("No HR+/HER2- breast cancer"), function.evaluate(
                TumorTestFactory.withPriorMolecularTestsAndDoids(
                    listOf(
                        createPriorMolecularTest("ER", "Negative"),
                        createPriorMolecularTest("PR", "Negative"),
                        createPriorMolecularTest("HER2", "Positive")
                    ),
                    setOf(
                        DoidConstants.BREAST_CANCER_DOID
                    )
                )
            ).failGeneralMessages
        )
    }

    @Test
    fun `Should evaluate undetermined when unclear status based on doids and IHC data missing`() {
        Assert.assertEquals(
            setOf("Undetermined HR/Her2-status since IHC data missing"), function.evaluate(
                TumorTestFactory.withPriorMolecularTestsAndDoids(
                    emptyList(),
                    setOf(
                        DoidConstants.BREAST_CANCER_DOID
                    )
                )
            ).undeterminedGeneralMessages
        )
    }

    @Test
    fun `Can compare molecular and clinical HER2 status`() {
        val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
        val function = HasBreastCancerHormonePositiveHER2Negative(doidModel)
        val match = setOf(
            DoidConstants.BREAST_CANCER_DOID,
            DoidConstants.HER2_NEGATIVE_BREAST_CANCER_DOID,
            DoidConstants.PROGESTERONE_POSITIVE_BREAST_CANCER_DOID,
            DoidConstants.ESTROGEN_POSITIVE_BREAST_CANCER_DOID
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withDoidsAndAmplification(match, "KRAS")))
        assertEvaluation(EvaluationResult.WARN, function.evaluate(TumorTestFactory.withDoidsAndAmplification(match, "ERBB2")))
    }

    companion object {
        private fun createPriorMolecularTest(item: String, score: String): PriorMolecularTest {
            return PriorMolecularTest(test = "IHC", item = item, scoreText = score, impliesPotentialIndeterminateStatus = false)
        }
    }
}