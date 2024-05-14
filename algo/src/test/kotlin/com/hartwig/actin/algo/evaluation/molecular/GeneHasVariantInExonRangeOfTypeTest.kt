package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.molecular.datamodel.driver.TestTranscriptImpactFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import com.hartwig.actin.molecular.datamodel.driver.VariantType
import com.hartwig.actin.trial.input.datamodel.VariantTypeInput
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val MATCHING_EXON = 1
private const val OTHER_EXON = 6
private const val TARGET_GENE = "gene A"


class GeneHasVariantInExonRangeOfTypeTest {
    private val function = GeneHasVariantInExonRangeOfType(TARGET_GENE, MATCHING_EXON, 2, VariantTypeInput.INSERT)

    @Test
    fun `Should fail when gene not present`() {
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }

    @Test
    fun `Should fail when no exons configured`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withVariant(TestVariantFactory.createMinimal().copy(isReportable = true, gene = TARGET_GENE))
            )
        )
    }

    @Test
    fun `Should fail when no variant type configured`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = TARGET_GENE, isReportable = true, canonicalImpact = impactWithExon(MATCHING_EXON)
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail when no exons match`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = TARGET_GENE, isReportable = true, type = VariantType.INSERT, canonicalImpact = impactWithExon(OTHER_EXON)
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail with wrong variant type`() {
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = TARGET_GENE, isReportable = true, type = VariantType.MNV, canonicalImpact = impactWithExon(MATCHING_EXON)
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass with correct gene, correct exon, correct variant type, and canonical`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = TARGET_GENE, isReportable = true, type = VariantType.INSERT, canonicalImpact = impactWithExon(MATCHING_EXON)
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn with correct gene, correct exon, correct variant type, canonical, but not reportable`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = TARGET_GENE, isReportable = false, type = VariantType.INSERT, canonicalImpact = impactWithExon(MATCHING_EXON)
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn with correct gene, correct exon, correct variant type, but only non-canonical matches`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = TARGET_GENE,
                        isReportable = true,
                        type = VariantType.INSERT,
                        canonicalImpact = impactWithExon(OTHER_EXON),
                        otherImpacts = setOf(impactWithExon(OTHER_EXON), impactWithExon(MATCHING_EXON))
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail for type INDEL when type is MNV`() {
        val function = GeneHasVariantInExonRangeOfType(TARGET_GENE, MATCHING_EXON, 2, VariantTypeInput.INDEL)
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = TARGET_GENE, isReportable = true, type = VariantType.MNV, canonicalImpact = impactWithExon(MATCHING_EXON)
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass for type INDEL when type is INSERT`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = TARGET_GENE, isReportable = true, type = VariantType.INSERT, canonicalImpact = impactWithExon(MATCHING_EXON)
                    )
                )
            )
        )
    }

    @Test
    fun `Should evaluate for all variant input types`() {
        for (input in VariantTypeInput.values()) {
            val function = GeneHasVariantInExonRangeOfType(TARGET_GENE, MATCHING_EXON, 2, input)
            assertThat(function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord())).isNotNull()
        }
    }

    @Test
    fun `Should evaluate without variant types`() {
        val function = GeneHasVariantInExonRangeOfType(TARGET_GENE, MATCHING_EXON, 2, null)
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = TARGET_GENE, isReportable = true, canonicalImpact = impactWithExon(MATCHING_EXON)
                    )
                )
            )
        )
    }

    private fun impactWithExon(affectedExon: Int) = TestTranscriptImpactFactory.createMinimal().copy(affectedExon = affectedExon)
}