package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.IHCMolecularTest
import com.hartwig.actin.molecular.datamodel.OtherPriorMolecularTest
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory.freeTextPriorMolecularFusionRecord
import com.hartwig.actin.molecular.datamodel.TestPanelRecordFactory
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherVariant
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanelType
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
                    OtherPriorMolecularTest(MolecularTestFactory.priorMolecularTest(item = "gene 1", impliesIndeterminate = true))
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
                    IHCMolecularTest(MolecularTestFactory.priorMolecularTest(test = "IHC", item = "gene 1", impliesIndeterminate = false))
                )
            )
        )
    }

    @Test
    fun `Should pass if no WGS or oncopanel has been performed but gene is in priorMolecularTest`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withMolecularTestsAndNoOrangeMolecular(
                    listOf(
                        IHCMolecularTest(
                            MolecularTestFactory.priorMolecularTest(
                                test = "IHC",
                                item = "gene 1",
                                impliesIndeterminate = false
                            )
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
                    ExperimentType.WHOLE_GENOME,
                    false,
                    OtherPriorMolecularTest(MolecularTestFactory.priorMolecularTest(item = "gene 2", impliesIndeterminate = false))
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
                    MolecularTestFactory.withMolecularTestsAndNoOrangeMolecular(
                        listOf(TestPanelRecordFactory.empty().copy(archerPanelExtraction = ArcherPanelExtraction()))
                    )
                )
        )
    }

    @Test
    fun `Should pass if gene is explicitly tested in Archer panel`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withMolecularTestsAndNoOrangeMolecular(
                    listOf(archerPanelWithVariantForGene("gene 1"))
                )
            )
        )
    }

    private fun archerPanelWithVariantForGene(gene: String) =
        TestPanelRecordFactory.empty().copy(archerPanelExtraction = ArcherPanelExtraction(variants = listOf(ArcherVariant(gene, "c.1A>T"))))

    @Test
    fun `Should fail for Archer if gene is not tested in panel`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withMolecularTestsAndNoOrangeMolecular(
                    listOf(archerPanelWithVariantForGene("gene 2"))
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
                    MolecularTestFactory.withMolecularTestsAndNoOrangeMolecular(
                        listOf(
                            TestPanelRecordFactory.empty().copy(genericPanelExtraction = GenericPanelExtraction(GenericPanelType.AVL))
                        )
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

    @Test
    fun `Should pass for gene in fusion curated from free text`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withMolecularTestsAndNoOrangeMolecular(
                    listOf(
                        freeTextPriorMolecularFusionRecord("gene 1", "gene 2")
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail for gene not in fusion curated from free text`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withMolecularTestsAndNoOrangeMolecular(
                    listOf(
                        freeTextPriorMolecularFusionRecord("gene 2", "gene 3")
                    )
                )
            )
        )
    }
}
