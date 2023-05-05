package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasBreastCancerHormonePositiveHER2NegativeTest {
    @Test
    fun canEvaluate() {
        val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
        val function = HasBreastCancerHormonePositiveHER2Negative(doidModel)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withDoids(null)))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids("wrong")))
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TumorTestFactory.withDoids(
                    DoidConstants.HER2_NEGATIVE_BREAST_CANCER_DOID,
                    DoidConstants.PROGESTERONE_POSITIVE_BREAST_CANCER_DOID
                )
            )
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TumorTestFactory.withDoids(
                    DoidConstants.HER2_NEGATIVE_BREAST_CANCER_DOID,
                    DoidConstants.ESTROGEN_POSITIVE_BREAST_CANCER_DOID
                )
            )
        )
        assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(TumorTestFactory.withDoids(DoidConstants.ESTROGEN_POSITIVE_BREAST_CANCER_DOID))
        )
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withDoids(DoidConstants.BREAST_CANCER_DOID)))
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                TumorTestFactory.withDoids(
                    DoidConstants.BREAST_CANCER_DOID,
                    DoidConstants.HER2_POSITIVE_BREAST_CANCER_DOID
                )
            )
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                TumorTestFactory.withDoids(
                    DoidConstants.BREAST_CANCER_DOID,
                    DoidConstants.ESTROGEN_NEGATIVE_BREAST_CANCER_DOID
                )
            )
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                TumorTestFactory.withDoids(
                    DoidConstants.BREAST_CANCER_DOID,
                    DoidConstants.PROGESTERONE_NEGATIVE_BREAST_CANCER_DOID
                )
            )
        )
    }

    @Test
    fun canCompareMolecularAndClinicalHer2Status() {
        val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
        val function = HasBreastCancerHormonePositiveHER2Negative(doidModel)
        val match = setOf(DoidConstants.HER2_NEGATIVE_BREAST_CANCER_DOID, DoidConstants.PROGESTERONE_POSITIVE_BREAST_CANCER_DOID)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withDoidsAndAmplication(match, "KRAS")))
        assertEvaluation(EvaluationResult.WARN, function.evaluate(TumorTestFactory.withDoidsAndAmplication(match, "ERBB2")))
    }
}