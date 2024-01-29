package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.molecular.datamodel.ExperimentType
import org.junit.Test

class MolecularResultsAreAvailableForGeneTest {
    private val function = MolecularResultsAreAvailableForGene("gene 1")
    
    @Test
    fun canEvaluate() {
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(MolecularTestFactory.withExperimentTypeAndContainingTumorCells(ExperimentType.WHOLE_GENOME, true))
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(MolecularTestFactory.withExperimentTypeAndContainingTumorCells(ExperimentType.WHOLE_GENOME, false))
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withExperimentTypeAndContainingTumorCells(ExperimentType.TARGETED, true))
        )
        assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withExperimentTypeAndPriorTest(
                    ExperimentType.TARGETED,
                    MolecularTestFactory.priorMolecularTest(item = "gene 1", impliesIndeterminate = false)
                )
            )
        )
        assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withExperimentTypeAndPriorTest(
                    ExperimentType.TARGETED,
                    MolecularTestFactory.priorMolecularTest(item = "gene 1", impliesIndeterminate = true)
                )
            )
        )
        assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withExperimentTypeAndPriorTest(
                    ExperimentType.TARGETED,
                    MolecularTestFactory.priorMolecularTest(item = "gene 2", impliesIndeterminate = false)
                )
            )
        )
    }

}