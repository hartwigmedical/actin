package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.TestFusionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptVariantImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val MATCHING_GENE = "gene_A"
private const val EXON_TO_SKIP = 2

private val EXON_SKIPPING_FUSION = TestFusionFactory.createMinimal().copy(
    isReportable = true,
    geneStart = MATCHING_GENE,
    geneEnd = MATCHING_GENE,
    fusedExonUp = EXON_TO_SKIP - 1,
    fusedExonDown = EXON_TO_SKIP + 1
)

private val SPLICE_VARIANT = TestVariantFactory.createMinimal().copy(
    gene = MATCHING_GENE,
    isReportable = true,
    canonicalImpact = TestTranscriptVariantImpactFactory.createMinimal()
        .copy(affectedExon = EXON_TO_SKIP, codingEffect = CodingEffect.SPLICE)
)

private val POTENTIAL_SPLICE_VARIANT =
    SPLICE_VARIANT.copy(
        event = "c.potential",
        canonicalImpact = TestTranscriptVariantImpactFactory.createMinimal()
            .copy(affectedExon = EXON_TO_SKIP, inSpliceRegion = true, codingEffect = CodingEffect.NONE)
    )

class GeneHasSpecificExonSkippingTest {

    val function = GeneHasSpecificExonSkipping(MATCHING_GENE, 2)

    @Test
    fun `Should be undetermined when no molecular history in patient record`() {
        assertMolecularEvaluation(
            EvaluationResult.UNDETERMINED, function.evaluate(
                TestPatientFactory.createEmptyMolecularTestPatientRecord()
            )
        )
    }

    @Test
    fun `Should fail when no variants in patient record`() {
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }

    @Test
    fun `Should warn on splice variant in specific exon`() {
        assertMolecularEvaluation(EvaluationResult.WARN, function.evaluate(MolecularTestFactory.withVariant(SPLICE_VARIANT)))
    }

    @Test
    fun `Should pass when exon skipping is confirmed for splice variant in specific exon`() {
        val confirmedVariant = SPLICE_VARIANT.copy(exonSkippingIsConfirmed = true)

        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withVariant(confirmedVariant)))
    }

    @Test
    fun `Should fail on splice variant in specific exon that is not reportable`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withVariant(SPLICE_VARIANT.copy(isReportable = false)))
        )
    }

    @Test
    fun `Should warn on splice variant in specific exon that is not a splice effect variant but is in splice region`() {
        val result = function.evaluate(MolecularTestFactory.withVariant(POTENTIAL_SPLICE_VARIANT))

        assertMolecularEvaluation(EvaluationResult.WARN, result)
        assertThat(result.warnMessagesStrings())
            .containsExactly("Potential gene_A exon 2 skipping: variant(s) c.potential detected in splice region of exon 2 although unknown relevance (not annotated with splice coding effect)")
    }

    @Test
    fun `Should pass on fusion skipping specific exon`() {
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withFusion(EXON_SKIPPING_FUSION)))
    }

    @Test
    fun `Should fail on fusion skipping specific exon that is not reportable`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withFusion(EXON_SKIPPING_FUSION.copy(isReportable = false)))
        )
    }

    @Test
    fun `Should fail on fusion skipping more than the specific exon`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withFusion(
                    EXON_SKIPPING_FUSION.copy(fusedExonDown = 5)
                )
            )
        )
    }

    @Test
    fun `Should warn when fusion skipping but also splice variant`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withDrivers(EXON_SKIPPING_FUSION, SPLICE_VARIANT)
            )
        )
    }

    @Test
    fun `Should evaluate undetermined with appropriate message when target coverage insufficient`() {
        val result = function.evaluate(
            TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
                molecularTests = listOf(TestMolecularFactory.createMinimalPanelTest())
            )
        )
        assertThat(result.result).isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(result.undeterminedMessagesStrings())
            .containsExactly("Skipped exon 2 in gene gene_A undetermined (not tested for mutations or fusions)")
    }
}
