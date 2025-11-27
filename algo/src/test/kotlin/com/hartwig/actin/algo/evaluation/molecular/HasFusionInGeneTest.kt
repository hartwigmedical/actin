package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.algo.evaluation.IhcTestEvaluationConstants
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.FusionDriverType
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestFusionFactory
import com.hartwig.actin.molecular.util.GeneConstants
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val MATCHING_GENE = "gene A"
private val MATCHING_GENE_IHC = GeneConstants.IHC_FUSION_EVALUABLE_GENES.first()

class HasFusionInGeneTest {

    val function = HasFusionInGene(MATCHING_GENE)
    val ihcFunction = HasFusionInGene(MATCHING_GENE_IHC)

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
    fun `Should warn with matching IHC result`() {
        val result = ihcFunction.evaluate(
            MolecularTestFactory.withIhcTests(
                IhcTest(
                    MATCHING_GENE_IHC,
                    scoreText = IhcTestEvaluationConstants.POSITIVE_TERMS.first()
                )
            )
        )
        assertMolecularEvaluation(EvaluationResult.WARN, result)
        assertThat(result.warnMessagesStrings()).containsExactly("ALK IHC result(s) may indicate ALK fusion")
    }

    @Test
    fun `Should fail with positive IHC result but gene cannot be evaluated by IHC`() {
        val result = function.evaluate(
            MolecularTestFactory.withIhcTests(
                IhcTest(
                    MATCHING_GENE,
                    scoreText = IhcTestEvaluationConstants.POSITIVE_TERMS.first()
                )
            )
        )

        assertMolecularEvaluation(EvaluationResult.FAIL, result)
        assertThat(result.failMessagesStrings()).containsExactly("No fusion in gene A")
    }

    @Test
    fun `Should warn with indeterminate IHC result`() {
        val test = IhcTest(
            MATCHING_GENE_IHC,
            scoreText = IhcTestEvaluationConstants.POSITIVE_TERMS.first(),
            impliesPotentialIndeterminateStatus = true
        )
        //val result = ihcFunction.evaluate(MolecularTestFactory.withIhcTests(test))
        val resultOnlyIhcTests = ihcFunction.evaluate(MolecularTestFactory.withOnlyIhcTests(listOf(test)))

        //assertMolecularEvaluation(EvaluationResult.WARN, result)
        assertMolecularEvaluation(EvaluationResult.WARN, resultOnlyIhcTests)

        val message = "ALK IHC result(s) are indeterminate - undetermined if this may indicate ALK fusion"
        //assertThat(result.warnMessagesStrings()).containsExactly(message)
        assertThat(resultOnlyIhcTests.warnMessagesStrings()).containsExactly(message)
    }

    @Test
    fun `Should fail with negative IHC result`() {
        val test = IhcTest(MATCHING_GENE_IHC, scoreText = IhcTestEvaluationConstants.BROAD_NEGATIVE_TERMS.first())
        val result = ihcFunction.evaluate(MolecularTestFactory.withIhcTests(test))
        val resultOnlyIhcTests = ihcFunction.evaluate(MolecularTestFactory.withOnlyIhcTests(listOf(test)))

        assertMolecularEvaluation(EvaluationResult.FAIL, result)
        assertMolecularEvaluation(EvaluationResult.FAIL, resultOnlyIhcTests)

        val message = "No fusion in $MATCHING_GENE_IHC"
        assertThat(result.failMessagesStrings()).containsExactly(message)
        assertThat(resultOnlyIhcTests.failMessagesStrings()).containsExactly(message)
    }

    @Test
    fun `Should evaluate undetermined with appropriate message when target coverage insufficient`() {
        val result = function.evaluate(
            TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
                molecularTests = listOf(TestMolecularFactory.createMinimalPanelTest())
            )
        )
        Assertions.assertThat(result.result).isEqualTo(EvaluationResult.UNDETERMINED)
        Assertions.assertThat(result.undeterminedMessagesStrings())
            .containsExactly("Fusion in gene gene A undetermined (not tested for fusions)")
    }
}