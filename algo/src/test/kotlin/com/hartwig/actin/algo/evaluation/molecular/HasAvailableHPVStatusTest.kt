package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.molecular.datamodel.ExperimentType
import org.junit.Test

class HasAvailableHPVStatusTest{

    private val function = HasAvailableHPVStatus()

    @Test
    fun `Should pass when WGS test contains sufficient tumor cells`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withExperimentTypeAndContainingTumorCells(
                    ExperimentType.WHOLE_GENOME, true
                )
            )
        )
    }

    @Test
    fun `Should resolve to undetermined if WGS does not contain enough tumor cells and no correct test in prior molecular tests `() {
        val record = MolecularTestFactory.withExperimentTypeAndContainingTumorCellsAndPriorTest(
            ExperimentType.WHOLE_GENOME, false, MolecularTestFactory.priorMolecularTest(test = "IHC", item = "Something"))
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record))
    }

    @Test
    fun `Should resolve to undetermined if no WGS has been performed and correct test is in priorMolecularTest with indeterminate status`() {
        val record = MolecularTestFactory.withExperimentTypeAndContainingTumorCellsAndPriorTest(
            ExperimentType.WHOLE_GENOME, false, MolecularTestFactory.priorMolecularTest(
                test = "IHC", item = "HPV", impliesIndeterminate = true)
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record))
    }

    @Test
    fun `Should pass if WGS does not contain enough tumor cells but correct test is in priorMolecularTest`() {
        val record = MolecularTestFactory.withExperimentTypeAndContainingTumorCellsAndPriorTest(
            ExperimentType.WHOLE_GENOME, false, MolecularTestFactory.priorMolecularTest(
                test = "IHC", item = "HPV", impliesIndeterminate = false)
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should pass if no WGS performed but correct test is in priorMolecularTest`() {
        val record = MolecularTestFactory.withPriorTest(MolecularTestFactory.priorMolecularTest(
                test = "IHC", item = "HPV", impliesIndeterminate = false)
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should evaluate to undetermined if no WGS performed and correct item not in prior molecular tests`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withExperimentTypeAndContainingTumorCellsAndPriorTest(
                    ExperimentType.WHOLE_GENOME,
                    false,
                    MolecularTestFactory.priorMolecularTest(item = "Something", impliesIndeterminate = false)
                )
            )
        )
    }
}