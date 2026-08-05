package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.configuration.ReportIntendedUse
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.ExperimentType
import org.junit.jupiter.api.Test

class CanProvideFreshSampleForFurtherAnalysisTest {

    private val labels = EvaluationLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY).tumor

    @Test
    fun canEvaluate() {
        val function = CanProvideFreshSampleForFurtherAnalysis(labels)
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