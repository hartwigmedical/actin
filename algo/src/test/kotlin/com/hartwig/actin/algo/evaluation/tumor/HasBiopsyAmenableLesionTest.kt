package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.molecular.datamodel.ExperimentType
import org.junit.Test

class HasBiopsyAmenableLesionTest {
    @Test
    fun canEvaluate() {
        val function = HasBiopsyAmenableLesion()
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TumorTestFactory.withMolecularExperimentType(ExperimentType.TARGETED))
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TumorTestFactory.withMolecularExperimentType(ExperimentType.WHOLE_GENOME))
        )
    }
}