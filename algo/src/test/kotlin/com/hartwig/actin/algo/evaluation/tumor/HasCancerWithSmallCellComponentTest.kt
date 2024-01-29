package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.doid.TestDoidModelFactory
import org.junit.Test

class HasCancerWithSmallCellComponentTest {

    @Test
    fun canEvaluate() {
        val doidModel = TestDoidModelFactory.createWithOneDoidAndTerm(
            "matching doid",
            HasCancerWithSmallCellComponent.SMALL_CELL_TERMS.iterator().next()
        )
        val function = HasCancerWithSmallCellComponent(doidModel)
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withTumorDetails(TumorDetails()))
        )
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TumorTestFactory.withDoids("wrong")))
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TumorTestFactory.withDoids("matching doid")))
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withDoids(HasCancerWithSmallCellComponent.SMALL_CELL_DOIDS.iterator().next()))
        )
        val extraDetails = HasCancerWithSmallCellComponent.SMALL_CELL_EXTRA_DETAILS.iterator().next() + " tumor"
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withTumorDetails(TumorDetails(primaryTumorExtraDetails = extraDetails)))
        )
    }
}