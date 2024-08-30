package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.ExperimentType
import org.junit.Test

class HasBiopsyAmenableLesionTest {
    @Test
    fun canEvaluate() {
        val function = HasBiopsyAmenableLesion()
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withMolecularExperimentType(ExperimentType.HARTWIG_TARGETED))
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withMolecularExperimentType(ExperimentType.HARTWIG_WHOLE_GENOME))
        )
    }
}