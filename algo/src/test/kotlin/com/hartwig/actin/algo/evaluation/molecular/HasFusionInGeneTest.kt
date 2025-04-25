package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestFusionFactory
import com.hartwig.actin.datamodel.molecular.driver.FusionDriverType
import org.assertj.core.api.Assertions
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
    fun `Should warn on matching high driver reportable gain of function fusion when non-reportable fusion also present`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                MolecularTestFactory.withDrivers(
                    matchingFusion,
                    matchingFusion.copy(isReportable = false)
                )
            )
        )
    }

    @Test
    fun `Should warn on matching high driver reportable gain of function fusion when non-high driver likelihood gain of function fusion also present`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                MolecularTestFactory.withDrivers(
                    matchingFusion,
                    matchingFusion.copy(driverLikelihood = DriverLikelihood.LOW)
                )
            )
        )
    }

    @Test
    fun `Should warn on matching high driver reportable gain of function fusion when fusion with no effect also present`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                MolecularTestFactory.withDrivers(
                    matchingFusion,
                    matchingFusion.copy(proteinEffect = ProteinEffect.NO_EFFECT)
                )
            )
        )
    }

    @Test
    fun `Should warn on matching high driver reportable gain of function fusion when non-gain of function and non-high driver likelihood fusion present`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN, function.evaluate(
                MolecularTestFactory.withDrivers(
                    matchingFusion,
                    matchingFusion.copy(proteinEffect = ProteinEffect.NO_EFFECT, driverLikelihood = DriverLikelihood.LOW)
                )
            )
        )
    }

    @Test
    fun `Should evaluate undetermined with appropriate message when target coverage insufficient`() {
        val result = function.evaluate(
            TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
                molecularHistory = MolecularHistory(molecularTests = listOf(TestMolecularFactory.createMinimalTestPanelRecord()))
            )
        )
        Assertions.assertThat(result.result).isEqualTo(EvaluationResult.UNDETERMINED)
        Assertions.assertThat(result.undeterminedMessages)
            .containsExactly("Fusion in gene gene A undetermined (not tested for fusions)")
    }
}