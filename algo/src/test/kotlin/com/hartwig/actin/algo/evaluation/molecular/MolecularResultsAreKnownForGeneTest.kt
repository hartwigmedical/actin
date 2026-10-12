package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptCopyNumberImpactFactory
import org.junit.jupiter.api.Test

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
                MolecularTestFactory.withExperimentTypeAndHasSufficientQuality(
                    ExperimentType.HARTWIG_WHOLE_GENOME, true
                )
            ),
            "WGS results available for gene 1"
        )
    }

    @Test
    fun `Should resolve to undetermined if WGS does not contain enough tumor cells`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withExperimentTypeAndHasSufficientQuality(
                    ExperimentType.HARTWIG_WHOLE_GENOME, false
                )
            ),
            "WGS performed containing gene 1 but biopsy contained insufficient tumor cells for analysis"
        )
    }

    @Test
    fun `Should warn if oncopanel contains sufficient tumor cells and not sure if gene is tested for`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withExperimentTypeAndHasSufficientQuality(
                    ExperimentType.HARTWIG_TARGETED, true
                )
            ),
            "Unsure if gene gene 1 results are available within performed OncoAct tumor NGS panel"
        )
    }

    @Test
    fun `Should resolve to undetermined if oncopanel does not contain enough tumor cells`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withExperimentTypeAndHasSufficientQuality(
                    ExperimentType.HARTWIG_TARGETED, false
                )
            ),
            "OncoAct tumor NGS panel performed containing gene 1 but biopsy contained insufficient tumor cells for analysis"
        )
    }

    @Test
    fun `Should pass if oncopanel has gene copy number for the gene`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withExperimentTypeAndCopyNumber(ExperimentType.HARTWIG_TARGETED, geneCopyNumber1)
            ),
            "OncoAct tumor NGS panel results available for gene 1"
        )
    }

    @Test
    fun `Should warn if oncopanel has no gene copy number for the gene`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withExperimentTypeAndCopyNumber(ExperimentType.HARTWIG_TARGETED, geneCopyNumber2)
            ),
            "Unsure if gene gene 1 results are available within performed OncoAct tumor NGS panel"
        )
    }

    @Test
    fun `Should resolve to undetermined if no WGS or oncopanel has been performed but gene is in molecularTest with indeterminate status`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withExperimentTypeAndHasSufficientQualityAndPriorTest(
                    ExperimentType.HARTWIG_WHOLE_GENOME,
                    false,
                    MolecularTestFactory.ihcTest(item = "gene 1", impliesIndeterminate = true)
                )
            ),
            "WGS performed containing gene 1 but biopsy contained insufficient tumor cells for analysis"
        )
    }

    @Test
    fun `Should pass if no successful WGS or oncopanel has been performed but gene is in molecularTest`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withExperimentTypeAndHasSufficientQuality(
                    ExperimentType.HARTWIG_WHOLE_GENOME,
                    false

                ).copy(
                    ihcTests = listOf(
                        MolecularTestFactory.ihcTest(
                            item = "gene 1",
                            impliesIndeterminate = false
                        )
                    )
                )
            ),
            "gene 1 tested before in IHC test"
        )
    }

    @Test
    fun `Should pass if no WGS or oncopanel has been performed but gene is in molecularTest`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
                    ihcTests =
                        listOf(
                            MolecularTestFactory.ihcTest(
                                item = "gene 1",
                                impliesIndeterminate = false
                            )
                        )
                )
            ),
            "WGS results available for gene 1"
        )
    }

    @Test
    fun `Should resolve to undetermined if no data is available for any tests for this gene`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withExperimentTypeAndHasSufficientQualityAndPriorTest(
                    ExperimentType.HARTWIG_WHOLE_GENOME,
                    false,
                    MolecularTestFactory.ihcTest(item = "gene 2", impliesIndeterminate = false)
                )
            ),
            "WGS performed containing gene 1 but biopsy contained insufficient tumor cells for analysis"
        )
    }

    @Test
    fun `Should pass for gene that is marked as tested in panel molecular test`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            MolecularResultsAreKnownForGene("ALK")
                .evaluate(
                    MolecularTestFactory.withMolecularTestsAndNoOrangeMolecular(
                        listOf(
                            TestMolecularFactory.createMinimalPanelTest()
                                .copy(targetSpecification = TestMolecularFactory.panelSpecifications(setOf("ALK")))
                        )
                    )
                ),
            "Panel results available for ALK"
        )
    }


    @Test
    fun `Should fail for gene that is not marked as tested in panel molecular test`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            MolecularResultsAreKnownForGene("ALK")
                .evaluate(
                    MolecularTestFactory.withMolecularTestsAndNoOrangeMolecular(
                        listOf(
                            TestMolecularFactory.createMinimalPanelTest()
                                .copy(targetSpecification = TestMolecularFactory.panelSpecifications(setOf("EGFR")))
                        )
                    )
                ),
            "ALK not tested"
        )
    }


    @Test
    fun `Should fail for generic panel if gene is not tested in panel`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withMolecularTestsAndNoOrangeMolecular(
                    listOf(TestMolecularFactory.createMinimalPanelTest())
                )
            ),
            "gene 1 not tested"
        )
    }
}
