package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.algo.evaluation.molecular.MolecularTestFactory.addingTestFromPriorMolecular
import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.Drivers
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory.freeTextPriorMolecularFusionRecord
import com.hartwig.actin.molecular.datamodel.TestPanelRecordFactory
import com.hartwig.actin.molecular.datamodel.driver.TestFusionFactory
import com.hartwig.actin.molecular.datamodel.orange.driver.FusionDriverType
import com.hartwig.actin.molecular.datamodel.panel.PanelFusionExtraction
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanelExtraction
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val MATCHING_GENE = "gene A"

class HasFusionInGeneTest {
    val function = HasFusionInGene(MATCHING_GENE)

    private val matchingFusion = TestFusionFactory.createMinimal().copy(
        geneStart = MATCHING_GENE,
        isReportable = true,
        driverLikelihood = DriverLikelihood.HIGH,
        proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
        driverType = FusionDriverType.PROMISCUOUS_5
    )

    @Test
    fun `Should fail on minimal test patient record`() {
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }

    @Test
    fun `Should pass on high driver reportable gain of function matching fusion`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withFusion(matchingFusion))
        )
    }

    @Test
    fun `Should fail on three gene match when type five promiscuous`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withFusion(matchingFusion.copy(geneStart = "gene B", geneEnd = "gene A")))
        )
    }

    @Test
    fun `Should fail if exon del dup on different gene`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withFusion(matchingFusion.copy(geneStart = "gene B", geneEnd = "gene B")))
        )
    }

    @Test
    fun `Should fail on five gene match when type is three promiscuous`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withFusion(matchingFusion.copy(driverType = FusionDriverType.PROMISCUOUS_3)))
        )
    }

    @Test
    fun `Should warn on unreportable gain of function match`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(MolecularTestFactory.withFusion(matchingFusion.copy(isReportable = false)))
        )
    }

    @Test
    fun `Should fail on unreportable fusion with no effect`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withFusion(matchingFusion.copy(isReportable = false, proteinEffect = ProteinEffect.NO_EFFECT))
            )
        )
    }

    @Test
    fun `Should warn on low driver gain of function fusion`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(MolecularTestFactory.withFusion(matchingFusion.copy(driverLikelihood = DriverLikelihood.LOW)))
        )
    }

    @Test
    fun `Should warn on high driver fusion with no effect`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(MolecularTestFactory.withFusion(matchingFusion.copy(proteinEffect = ProteinEffect.NO_EFFECT)))
        )
    }

    @Test
    fun `Should pass on fusion in generic panel when no Orange molecular`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withMolecularTestsAndNoOrangeMolecular(
                    listOf(
                        freeTextPriorMolecularFusionRecord(MATCHING_GENE, "gene B").copy(
                            drivers = Drivers(fusions = setOf(matchingFusion))
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass on fusion in archer panel when no Orange molecular`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withMolecularTestsAndNoOrangeMolecular(
                    listOf(
                        TestPanelRecordFactory.empty().copy(
                            panelExtraction = ArcherPanelExtraction(fusions = listOf(PanelFusionExtraction(MATCHING_GENE, null))),
                            drivers = Drivers(fusions = setOf(matchingFusion))
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `Should prefer pass result from Orange molecular when fusion found in both Orange molecular and panels`() {
        val evaluation = function.evaluate(
            addingTestFromPriorMolecular(
                MolecularTestFactory.withFusion(matchingFusion),
                listOf(freeTextPriorMolecularFusionRecord(MATCHING_GENE, "gene B"))
            )
        )

        assertMolecularEvaluation(EvaluationResult.PASS, evaluation)
        assertThat(evaluation.passSpecificMessages).containsExactly("Fusion(s)  detected in gene gene A")
    }

    @Test
    fun `Should be undetermined for gene not tested in panel and no Orange molecular`() {
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED,
            function.evaluate(
                MolecularTestFactory.withMolecularTestsAndNoOrangeMolecular(
                    listOf(freeTextPriorMolecularFusionRecord("gene B", "gene C"))
                )
            )
        )
    }
}