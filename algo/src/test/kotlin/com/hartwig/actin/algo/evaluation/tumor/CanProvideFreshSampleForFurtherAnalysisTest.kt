package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.molecular.datamodel.ExperimentType
import org.junit.Test

class CanProvideFreshSampleForFurtherAnalysisTest {
    @Test
    fun canEvaluate() {
        val function = CanProvideFreshSampleForFurtherAnalysis()
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(TestTumorFactory.withMolecularExperimentType(ExperimentType.TARGETED))
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(TestTumorFactory.withMolecularExperimentType(ExperimentType.WHOLE_GENOME))
        )
    }
}