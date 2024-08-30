package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestFusionFactory
import com.hartwig.actin.datamodel.molecular.orange.driver.FusionDriverType
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
}