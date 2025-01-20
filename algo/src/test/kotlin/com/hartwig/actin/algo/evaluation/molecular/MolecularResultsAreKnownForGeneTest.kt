package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.ProteinEffect
import com.hartwig.actin.datamodel.molecular.TestPanelRecordFactory
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import org.junit.Test

class MolecularResultsAreKnownForGeneTest {
    private val function = MolecularResultsAreKnownForGene("gene 1")

    private val geneCopyNumber1 = TestCopyNumberFactory.createMinimal().copy(
        gene = "gene 1",
        isReportable = false,
        proteinEffect = ProteinEffect.UNKNOWN,
        canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact()
    )

    private val geneCopyNumber2 = TestCopyNumberFactory.createMinimal().copy(
        gene = "gene 2",
        isReportable = false,
        proteinEffect = ProteinEffect.UNKNOWN,
        canonicalImpact = TestTranscriptCopyNumberImpactFactory.createTranscriptCopyNumberImpact()
    )

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
    fun `Should resolve to undetermined if WGS does not contain enough tumor cells`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withExperimentTypeAndContainingTumorCells(
                    ExperimentType.HARTWIG_WHOLE_GENOME, false
                )
            )
        )
    }

    @Test
    fun `Should warn if oncopanel contains sufficient tumor cells and not sure if gene is tested for`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withExperimentTypeAndContainingTumorCells(
                    ExperimentType.HARTWIG_TARGETED, true
                )
            )
        )
    }

    @Test
    fun `Should resolve to undetermined if oncopanel does not contain enough tumor cells`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withExperimentTypeAndContainingTumorCells(
                    ExperimentType.HARTWIG_TARGETED, false
                )
            )
        )
    }

    @Test
    fun `Should pass if oncopanel has gene copy number for the gene`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withExperimentTypeAndCopyNumber(ExperimentType.HARTWIG_TARGETED, geneCopyNumber1)
            )
        )
    }

    @Test
    fun `Should warn if oncopanel has no gene copy number for the gene`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withExperimentTypeAndCopyNumber(ExperimentType.HARTWIG_TARGETED, geneCopyNumber2)
            )
        )
    }

    @Test
    fun `Should resolve to undetermined if no WGS or oncopanel has been performed but gene is in priorMolecularTest with indeterminate status`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withExperimentTypeAndContainingTumorCellsAndPriorTest(
                    ExperimentType.HARTWIG_WHOLE_GENOME,
                    false,
                    MolecularTestFactory.priorIHCTest(item = "gene 1", impliesIndeterminate = true)
                )
            )
        )
    }

    @Test
    fun `Should pass if no successful WGS or oncopanel has been performed but gene is in priorMolecularTest`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withExperimentTypeAndContainingTumorCells(
                    ExperimentType.HARTWIG_WHOLE_GENOME,
                    false

                ).copy(
                    priorIHCTests = listOf(
                        MolecularTestFactory.priorIHCTest(
                            test = "IHC",
                            item = "gene 1",
                            impliesIndeterminate = false
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass if no WGS or oncopanel has been performed but gene is in priorMolecularTest`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
                    priorIHCTests =
                    listOf(
                        MolecularTestFactory.priorIHCTest(
                            test = "IHC",
                            item = "gene 1",
                            impliesIndeterminate = false
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should resolve to undetermined if no data is available for any tests for this gene`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withExperimentTypeAndContainingTumorCellsAndPriorTest(
                    ExperimentType.HARTWIG_WHOLE_GENOME,
                    false,
                    MolecularTestFactory.priorIHCTest(item = "gene 2", impliesIndeterminate = false)
                )
            )
        )
    }

    @Test
    fun `Should pass for gene that is marked as tested in panel molecular test`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            MolecularResultsAreKnownForGene("ALK")
                .evaluate(
                    MolecularTestFactory.withMolecularTestsAndNoOrangeMolecular(
                        listOf(TestPanelRecordFactory.empty().copy(testedGenes = setOf("ALK")))
                    )
                )
        )
    }


    @Test
    fun `Should fail for gene that is not marked as tested in panel molecular test`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            MolecularResultsAreKnownForGene("ALK")
                .evaluate(
                    MolecularTestFactory.withMolecularTestsAndNoOrangeMolecular(
                        listOf(TestPanelRecordFactory.empty().copy(testedGenes = setOf("EGFR")))
                    )
                )
        )
    }


    @Test
    fun `Should fail for generic panel if gene is not tested in panel`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withMolecularTestsAndNoOrangeMolecular(
                    listOf(TestPanelRecordFactory.empty())
                )
            )
        )
    }
}
