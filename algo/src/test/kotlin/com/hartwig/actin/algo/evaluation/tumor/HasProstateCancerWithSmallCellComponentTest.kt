package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasProstateCancerWithSmallCellComponentTest {
    private val function = HasProstateCancerWithSmallCellComponent(
        TestDoidModelFactory.createWithOneParentChild(DoidConstants.PROSTATE_CANCER_DOID, DoidConstants.PROSTATE_SMALL_CELL_CARCINOMA_DOID)
    )
    
    @Test
    fun canEvaluate() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TumorTestFactory.withDoids(null)))
        val exact = TumorTestFactory.withDoids(DoidConstants.PROSTATE_SMALL_CELL_CARCINOMA_DOID)
        assertEvaluation(EvaluationResult.PASS, function.evaluate(exact))

        val smallCellHistology = TumorTestFactory.withDoidAndDetails(
            DoidConstants.PROSTATE_CANCER_DOID, HasProstateCancerWithSmallCellComponent.SMALL_CELL_DETAILS
        )
        assertEvaluation(EvaluationResult.PASS, function.evaluate(smallCellHistology))

        val warnProstateCancer: PatientRecord =
            TumorTestFactory.withDoids(HasProstateCancerWithSmallCellComponent.PROSTATE_WARN_DOID_SETS.iterator().next())
        assertEvaluation(EvaluationResult.WARN, function.evaluate(warnProstateCancer))

        val prostateCancer = TumorTestFactory.withDoids(DoidConstants.PROSTATE_CANCER_DOID)
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(prostateCancer))

        val somethingElse = TumorTestFactory.withDoids("something else")
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(somethingElse))
    }
}