package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.trial.input.datamodel.TumorTypeInput
import org.junit.Test

class HasCancerOfUnknownPrimaryTest {
    @Test
    fun canEvaluate() {
        val category = TumorTypeInput.ADENOCARCINOMA
        val childDoid = "child"
        val doidModel = TestDoidModelFactory.createWithOneParentChild(category.doid(), childDoid)
        val function = HasCancerOfUnknownPrimary(doidModel, category)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withDoids(null)))
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withDoids(DoidConstants.CANCER_DOID)))
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TumorTestFactory.withDoidAndSubLocation(
                    DoidConstants.CANCER_DOID,
                    HasCancerOfUnknownPrimary.CUP_PRIMARY_TUMOR_SUB_LOCATION
                )
            )
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids("random doid")))
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids(category.doid(), "other doid")))
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TumorTestFactory.withDoidAndSubLocation(
                    category.doid(),
                    HasCancerOfUnknownPrimary.CUP_PRIMARY_TUMOR_SUB_LOCATION
                )
            )
        )
        assertEvaluation(EvaluationResult.WARN, function.evaluate(TumorTestFactory.withDoids(category.doid())))
        assertEvaluation(EvaluationResult.WARN, function.evaluate(TumorTestFactory.withDoids(category.doid(), childDoid)))
    }
}