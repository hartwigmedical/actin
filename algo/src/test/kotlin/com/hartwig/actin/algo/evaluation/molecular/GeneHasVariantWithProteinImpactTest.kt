package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptVariantImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.TranscriptVariantImpact
import com.hartwig.actin.util.FormatFunctions
import org.assertj.core.api.Assertions
import org.junit.Test

private const val MATCHING_GENE = "gene A"

private const val MATCHING_PROTEIN_IMPACT = "p.V600E"

class GeneHasVariantWithProteinImpactTest {
    private val function = GeneHasVariantWithProteinImpact(MATCHING_GENE, setOf(FormatFunctions.removePrefix(MATCHING_PROTEIN_IMPACT), "V600K"))

    @Test
    fun `Should fail when gene not present`() {
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }

    @Test
    fun `Should fail when no protein impacts configured`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal()
                        .copy(
                            gene = MATCHING_GENE,
                            isReportable = true
                        )
                )
            )
        )
    }

    @Test
    fun `Should fail when no protein impacts match`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = MATCHING_GENE,
                        isReportable = true,
                        canonicalImpact = proteinImpact(FormatFunctions.removePrefix("p.V600P")),
                        otherImpacts = setOf(proteinImpact(FormatFunctions.removePrefix("p.V600P")))
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail for incorrect gene with matching impact`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = "gene B",
                        isReportable = true,
                        canonicalImpact = proteinImpact(FormatFunctions.removePrefix(MATCHING_PROTEIN_IMPACT))
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass for correct gene with matching canonical impact`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = MATCHING_GENE,
                        isReportable = true,
                        canonicalImpact = proteinImpact(FormatFunctions.removePrefix(MATCHING_PROTEIN_IMPACT))
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn for correct gene with matching canonical impact but not reportable`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = MATCHING_GENE,
                        isReportable = false,
                        canonicalImpact = proteinImpact(FormatFunctions.removePrefix(MATCHING_PROTEIN_IMPACT))
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn for correct gene with matching canonical impact but subclonal`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = MATCHING_GENE,
                        isReportable = true,
                        clonalLikelihood = 0.3,
                        canonicalImpact = proteinImpact(FormatFunctions.removePrefix(MATCHING_PROTEIN_IMPACT))
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn for correct gene with matching non-canonical impact`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = MATCHING_GENE,
                        isReportable = true,
                        canonicalImpact = proteinImpact(FormatFunctions.removePrefix("p.V600P")),
                        otherImpacts = setOf(proteinImpact(FormatFunctions.removePrefix("p.V600P")), proteinImpact(FormatFunctions.removePrefix(MATCHING_PROTEIN_IMPACT)))
                    )
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
        Assertions.assertThat(result.undeterminedMessagesStrings())
            .containsExactly("Mutation with protein impact(s) V600E, V600K in gene gene A undetermined (not tested for mutations)")
    }

    private fun proteinImpact(hgvsProteinImpact: String): TranscriptVariantImpact {
        return TestTranscriptVariantImpactFactory.createMinimal().copy(hgvsProteinImpact = hgvsProteinImpact)
    }
}