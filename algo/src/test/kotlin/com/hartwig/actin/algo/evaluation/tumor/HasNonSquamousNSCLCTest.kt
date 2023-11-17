package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasNonSquamousNSCLCTest {
    @Test
    fun canEvaluate() {
        val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
        val function = HasNonSquamousNSCLC(doidModel)
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withDoids(null)))

        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids("wrong")))

        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                TumorTestFactory.withDoids(
                    DoidConstants.LUNG_ADENOSQUAMOUS_CARCINOMA_DOID
                )
            )
        )

        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                TumorTestFactory.withDoids(
                    DoidConstants.LUNG_SQUAMOUS_CELL_CARCINOMA_DOID
                )
            )
        )

        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TumorTestFactory.withDoids(
                    DoidConstants.LUNG_ADENOCARCINOMA_DOID,
                )
            )
        )

        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TumorTestFactory.withDoids(
                    DoidConstants.LUNG_NON_SQUAMOUS_NON_SMALL_CARCINOMA_DOID,
                    "random DOID"
                )
            )
        )

        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TumorTestFactory.withDoids(
                    DoidConstants.LUNG_CANCER_DOID
                )
            )
        )

        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                TumorTestFactory.withDoids(
                    DoidConstants.LUNG_SARCOMA
                )
            )
        )

    }
}