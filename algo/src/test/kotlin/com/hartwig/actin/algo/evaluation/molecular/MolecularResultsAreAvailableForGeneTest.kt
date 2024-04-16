package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory.archerPriorMolecularNoFusionsFoundRecord
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory.archerPriorMolecularVariantRecord
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory.avlPanelPriorMolecularNoMutationsFoundRecord
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory.referralPriorMolecularFusionRecord
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory
import org.junit.Test

class MolecularResultsAreAvailableForGeneTest {
    private val function = MolecularResultsAreAvailableForGene("gene 1")

    private val geneCopyNumber1 = TestCopyNumberFactory.createMinimal().copy(
        gene = "gene 1",
        isReportable = false,
        proteinEffect = ProteinEffect.UNKNOWN,
        type = CopyNumberType.NONE
    )

    private val geneCopyNumber2 = TestCopyNumberFactory.createMinimal().copy(
        gene = "gene 2",
        isReportable = false,
        proteinEffect = ProteinEffect.UNKNOWN,
        type = CopyNumberType.NONE
    )

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
    fun `Should resolve to undetermined if WGS does not contain enough tumor cells`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withExperimentTypeAndContainingTumorCells(
                    ExperimentType.WHOLE_GENOME, false
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
                    ExperimentType.TARGETED, true
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
                    ExperimentType.TARGETED, false
                )
            )
        )
    }

    @Test
    fun `Should pass if oncopanel has gene copy number for the gene`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withExperimentTypeAndCopyNumber(ExperimentType.TARGETED, geneCopyNumber1)
            )
        )
    }

    @Test
    fun `Should warn if oncopanel has no gene copy number for the gene`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withExperimentTypeAndCopyNumber(ExperimentType.TARGETED, geneCopyNumber2)
            )
        )
    }

    @Test
    fun `Should resolve to undetermined if no WGS or oncopanel has been performed but gene is in priorMolecularTest with indeterminate status`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withExperimentTypeAndContainingTumorCellsAndPriorTest(
                    ExperimentType.WHOLE_GENOME,
                    false,
                    MolecularTestFactory.priorMolecularTest(item = "gene 1", impliesIndeterminate = true)
                )
            )
        )
    }

    @Test
    fun `Should pass if no successful WGS or oncopanel has been performed but gene is in priorMolecularTest`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withExperimentTypeAndContainingTumorCellsAndPriorTest(
                    ExperimentType.WHOLE_GENOME,
                    false,
                    MolecularTestFactory.priorMolecularTest(test = "IHC", item = "gene 1", impliesIndeterminate = false)
                )
            )
        )
    }

    @Test
    fun `Should pass if no WGS or oncopanel has been performed but gene is in priorMolecularTest`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withPriorTestsAndNoOrangeMolecular(
                    listOf(MolecularTestFactory.priorMolecularTest(test = "IHC", item = "gene 1", impliesIndeterminate = false))
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
                    ExperimentType.WHOLE_GENOME,
                    false,
                    MolecularTestFactory.priorMolecularTest(item = "gene 2", impliesIndeterminate = false)
                )
            )
        )
    }

    @Test
    fun `Should pass for gene that is always tested in Archer panel`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            MolecularResultsAreAvailableForGene("ALK")
                .evaluate(
                    MolecularTestFactory.withPriorTestsAndNoOrangeMolecular(
                        listOf(archerPriorMolecularNoFusionsFoundRecord())
                    )
                )
        )
    }

    @Test
    fun `Should pass if gene is explicitly tested in Archer panel`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withPriorTestsAndNoOrangeMolecular(
                    listOf(archerPriorMolecularVariantRecord("gene 1", "c.1A>T"))
                )
            )
        )
    }

    @Test
    fun `Should fail for Archer if gene is not tested in panel`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withPriorTestsAndNoOrangeMolecular(
                    listOf(archerPriorMolecularVariantRecord("gene 2", "c.1A>T"))
                )
            )
        )
    }

    @Test
    fun `Should pass for gene that is always tested in generic panel`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            MolecularResultsAreAvailableForGene("EGFR")
                .evaluate(
                    MolecularTestFactory.withPriorTestsAndNoOrangeMolecular(
                        listOf(avlPanelPriorMolecularNoMutationsFoundRecord())
                    )
                )
        )
    }

    @Test
    fun `Should fail for generic panel if gene is not tested in panel`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withPriorTestsAndNoOrangeMolecular(
                    listOf(avlPanelPriorMolecularNoMutationsFoundRecord())
                )
            )
        )
    }

    @Test
    fun `Should pass for gene in fusion curated from free text`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withPriorTestsAndNoOrangeMolecular(
                    listOf(referralPriorMolecularFusionRecord("gene 1", "gene 2")
                    )
                )
            )
        )
    }

    fun `Should fail for gene not in fusion curated from free text`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withPriorTestsAndNoOrangeMolecular(
                    listOf(referralPriorMolecularFusionRecord("gene 2", "gene 3")
                    )
                )
            )
        )
    }
}
