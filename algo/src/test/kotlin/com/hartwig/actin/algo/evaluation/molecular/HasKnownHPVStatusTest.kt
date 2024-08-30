package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.ExperimentType
import org.junit.Test

class HasKnownHPVStatusTest {

    private val function = HasKnownHPVStatus()

    @Test
    fun `Should pass when WGS test contains sufficient tumor cells`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withExperimentTypeAndContainingTumorCells(
                    ExperimentType.HARTWIG_WHOLE_GENOME, true
                )
            )
        )
    }

    @Test
    fun `Should resolve to undetermined if WGS does not contain enough tumor cells and no correct test in prior molecular tests `() {
        val record = MolecularTestFactory.withExperimentTypeAndContainingTumorCells(
            ExperimentType.HARTWIG_WHOLE_GENOME, false
        ).copy(
            priorIHCTests = listOf(MolecularTestFactory.priorIHCTest(test = "IHC", item = "Something"))
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record))
    }

    @Test
    fun `Should resolve to undetermined if no WGS has been performed and correct test is in priorMolecularTest with indeterminate status`() {
        val record = MolecularTestFactory.withExperimentTypeAndContainingTumorCells(
            ExperimentType.HARTWIG_WHOLE_GENOME, false
        ).copy(
            priorIHCTests =
            listOf(
                MolecularTestFactory.priorIHCTest(
                    test = "IHC", item = "HPV", impliesIndeterminate = true
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record))
    }

    @Test
    fun `Should pass if WGS does not contain enough tumor cells but correct test is in priorMolecularTest`() {
        val record = MolecularTestFactory.withExperimentTypeAndContainingTumorCells(
            ExperimentType.HARTWIG_WHOLE_GENOME, false
        ).copy(
            priorIHCTests = listOf(
                MolecularTestFactory.priorIHCTest(
                    test = "IHC", item = "HPV", impliesIndeterminate = false
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should pass if no WGS performed but correct test is in priorMolecularTest`() {
        val record = TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
            priorIHCTests = listOf(MolecularTestFactory.priorIHCTest(test = "IHC", item = "HPV", impliesIndeterminate = false))
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    @Test
    fun `Should evaluate to undetermined if no WGS performed and correct item not in prior molecular tests`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                TestPatientFactory.createEmptyMolecularTestPatientRecord().copy(
                    priorIHCTests = listOf(
                        MolecularTestFactory.priorIHCTest(
                            test = "IHC",
                            item = "Something",
                            impliesIndeterminate = false
                        )
                    )
                )
            )
        )
    }
}