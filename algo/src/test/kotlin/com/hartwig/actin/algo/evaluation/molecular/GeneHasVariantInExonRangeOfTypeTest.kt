package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.VariantType
import com.hartwig.actin.datamodel.molecular.driver.TestFusionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptVariantImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.trial.input.datamodel.VariantTypeInput
import org.assertj.core.api.Assertions
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
                        gene = TARGET_GENE,
                        isReportable = true,
                        type = VariantType.INSERT,
                        canonicalImpact = impactWithExon(OTHER_EXON),
                        extendedVariantDetails = TestVariantFactory.createMinimalExtended()
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
                        gene = TARGET_GENE,
                        isReportable = true,
                        driverLikelihood = DriverLikelihood.HIGH,
                        type = VariantType.INSERT,
                        canonicalImpact = impactWithExon(MATCHING_EXON),
                        extendedVariantDetails = TestVariantFactory.createMinimalExtended()
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
    fun `Should warn with correct gene, correct exon, correct variant type, canonical, but not high driver`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = TARGET_GENE,
                        isReportable = true,
                        driverLikelihood = DriverLikelihood.MEDIUM,
                        type = VariantType.INSERT,
                        canonicalImpact = impactWithExon(MATCHING_EXON)
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
    fun `Should fail for input type INDEL when variant type is MNV`() {
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
    fun `Should pass for input type INDEL when variant type is INSERT`() {
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = TARGET_GENE,
                        isReportable = true,
                        driverLikelihood = DriverLikelihood.HIGH,
                        type = VariantType.INSERT,
                        canonicalImpact = impactWithExon(MATCHING_EXON),
                        extendedVariantDetails = TestVariantFactory.createMinimalExtended()
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass for reportable exon skipping fusion when variant type is DELETE`() {
        val function = GeneHasVariantInExonRangeOfType(TARGET_GENE, 1, 4, VariantTypeInput.DELETE)
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withFusion(
                    TestFusionFactory.createMinimal().copy(
                        geneStart = TARGET_GENE,
                        geneEnd = TARGET_GENE,
                        isReportable = true,
                        driverLikelihood = DriverLikelihood.HIGH,
                        fusedExonUp = 2,
                        fusedExonDown = 3
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn for unreportable exon skipping fusion when variant type is DELETE`() {
        val function = GeneHasVariantInExonRangeOfType(TARGET_GENE, 1, 4, VariantTypeInput.DELETE)
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withFusion(
                    TestFusionFactory.createMinimal().copy(
                        geneStart = TARGET_GENE,
                        geneEnd = TARGET_GENE,
                        isReportable = false,
                        fusedExonUp = 2,
                        fusedExonDown = 3
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn when exon skipping fusion is non-high driver`() {
        val function = GeneHasVariantInExonRangeOfType(TARGET_GENE, 1, 4, VariantTypeInput.DELETE)
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withFusion(
                    TestFusionFactory.createMinimal().copy(
                        geneStart = TARGET_GENE,
                        geneEnd = TARGET_GENE,
                        isReportable = true,
                        driverLikelihood = DriverLikelihood.MEDIUM,
                        fusedExonUp = 2,
                        fusedExonDown = 3
                    )
                )
            )
        )
    }

    @Test
    fun `Should evaluate for all variant input types`() {
        for (input in VariantTypeInput.entries) {
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
                        gene = TARGET_GENE,
                        isReportable = true,
                        driverLikelihood = DriverLikelihood.HIGH,
                        canonicalImpact = impactWithExon(MATCHING_EXON),
                        extendedVariantDetails = TestVariantFactory.createMinimalExtended()
                    )
                )
            )
        )
    }


    @Test
    fun `Should warn when canonical variant match but also other reportable match`() {
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withDrivers(
                    TestVariantFactory.createMinimal().copy(
                        gene = TARGET_GENE,
                        isReportable = true,
                        type = VariantType.INSERT,
                        canonicalImpact = impactWithExon(MATCHING_EXON),
                        extendedVariantDetails = TestVariantFactory.createMinimalExtended()
                    ),
                    TestVariantFactory.createMinimal().copy(
                        gene = TARGET_GENE,
                        isReportable = true,
                        type = VariantType.INSERT,
                        canonicalImpact = impactWithExon(OTHER_EXON),
                        otherImpacts = setOf(impactWithExon(MATCHING_EXON)),
                        extendedVariantDetails = TestVariantFactory.createMinimalExtended()
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn when reportable exon skips but also other reportable matches`() {
        val function = GeneHasVariantInExonRangeOfType(TARGET_GENE, 1, 4, VariantTypeInput.DELETE)
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withDrivers(
                    TestVariantFactory.createMinimal().copy(
                        gene = TARGET_GENE,
                        isReportable = true,
                        type = VariantType.DELETE,
                        canonicalImpact = impactWithExon(OTHER_EXON),
                        otherImpacts = setOf(impactWithExon(MATCHING_EXON)),
                        extendedVariantDetails = TestVariantFactory.createMinimalExtended()
                    ),
                    TestFusionFactory.createMinimal().copy(
                        geneStart = TARGET_GENE,
                        geneEnd = TARGET_GENE,
                        isReportable = true,
                        fusedExonUp = 2,
                        fusedExonDown = 3
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass for variant with matching canonical and non-canonical impact`() {
        val function = GeneHasVariantInExonRangeOfType(TARGET_GENE, 1, 4, VariantTypeInput.DELETE)
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withDrivers(
                    TestVariantFactory.createMinimal().copy(
                        gene = TARGET_GENE,
                        isReportable = true,
                        type = VariantType.DELETE,
                        driverLikelihood = DriverLikelihood.HIGH,
                        canonicalImpact = impactWithExon(MATCHING_EXON),
                        otherImpacts = setOf(impactWithExon(MATCHING_EXON)),
                        extendedVariantDetails = TestVariantFactory.createMinimalExtended()
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
        assertThat(result.result).isEqualTo(EvaluationResult.UNDETERMINED)
        assertThat(result.undeterminedMessages).containsExactly("Mutation in exon range 1 to 2 of type insertion in gene gene A undetermined (not tested for at least mutations)")
    }

    private fun impactWithExon(affectedExon: Int) = TestTranscriptVariantImpactFactory.createMinimal().copy(affectedExon = affectedExon)
}