package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.Drivers
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory
import com.hartwig.actin.molecular.datamodel.TranscriptImpact
import com.hartwig.actin.molecular.datamodel.VariantType
import com.hartwig.actin.molecular.datamodel.driver.TestFusionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestTranscriptImpactFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import org.assertj.core.api.Assertions
import org.junit.Test

private const val CORRECT_GENE = "EGFR"
private const val CORRECT_VARIANT_GENE = CORRECT_GENE
private const val CORRECT_PROTEIN_IMPACT = "L858R"
private const val OTHER_CORRECT_VARIANT_GENE = "BRAF"
private const val OTHER_CORRECT_PROTEIN_IMPACT = "V600E"
private const val CORRECT_EXON_SKIPPING_GENE = "MET"
private const val CORRECT_EXON_SKIPPING_EXON = 14
private const val CORRECT_FUSION_GENE = "ALK"
private const val CORRECT_DELETION_GENE = "EGFR"
private const val CORRECT_DELETION_CODON = 19
private const val CORRECT_INSERTION_GENE = "EGFR"
private const val CORRECT_INSERTION_CODON = 20

private val BASE_VARIANT = TestVariantFactory.createMinimal().copy(
    gene = CORRECT_GENE,
    isReportable = true,
    driverLikelihood = DriverLikelihood.HIGH,
    proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
    extendedVariantDetails = TestVariantFactory.createMinimalExtended().copy(clonalLikelihood = 1.0)
)

private val BASE_FUSION = TestFusionFactory.createMinimal().copy(
    isReportable = true,
    geneStart = CORRECT_FUSION_GENE,
    geneEnd = "Fusion partner",
    driverLikelihood = DriverLikelihood.HIGH,
    extendedFusionDetails = TestFusionFactory.createMinimalExtended().copy(fusedExonUp = 5, fusedExonDown = 3)
)

private val BASE_EXON_SKIPPING_FUSION = BASE_FUSION.copy(
    geneStart = CORRECT_EXON_SKIPPING_GENE,
    geneEnd = CORRECT_EXON_SKIPPING_GENE,
    extendedFusionDetails = TestFusionFactory.createMinimalExtended()
        .copy(fusedExonUp = CORRECT_EXON_SKIPPING_EXON.minus(1), fusedExonDown = CORRECT_EXON_SKIPPING_EXON.plus(1))
)


class HasMolecularEventWithSocTargetedTherapyForNSCLCAvailableTest {

    private val function = HasMolecularEventWithSocTargetedTherapyForNSCLCAvailable(emptySet())

    @Test
    fun `Should fail when molecular record is empty`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestPatientFactory.createMinimalTestWGSPatientRecord()))
    }

    @Test
    fun `Should pass for activating mutation in correct gene`() {
        val evaluation = function.evaluate(
            MolecularTestFactory.withVariant(BASE_VARIANT)
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, evaluation)
        Assertions.assertThat(evaluation.passGeneralMessages).containsExactly("$CORRECT_GENE activating mutation(s)")
    }

    @Test
    fun `Should fail if activating mutation is in correct gene but this gene is in geneToIgnore`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            HasMolecularEventWithSocTargetedTherapyForNSCLCAvailable(setOf(CORRECT_GENE)).evaluate(
                MolecularTestFactory.withVariant(BASE_VARIANT)
            )
        )
    }

    @Test
    fun `Should warn for mutation in correct gene when uncertain if activating`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    BASE_VARIANT.copy(proteinEffect = ProteinEffect.UNKNOWN)
                )
            )
        )
    }

    @Test
    fun `Should fail for activating mutation in wrong gene`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    BASE_VARIANT.copy(gene = "Wrong")
                )
            )
        )
    }

    @Test
    fun `Should pass for correct variant with correct protein impact`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    BASE_VARIANT.copy(
                        proteinEffect = ProteinEffect.UNKNOWN,
                        canonicalImpact = proteinImpact(CORRECT_PROTEIN_IMPACT)
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass for multiple correct variants and should display correct message`() {
        val variants = setOf(
            BASE_VARIANT.copy(
                gene = CORRECT_VARIANT_GENE,
                proteinEffect = ProteinEffect.UNKNOWN,
                canonicalImpact = proteinImpact(CORRECT_PROTEIN_IMPACT)
            ),
            BASE_VARIANT.copy(
                gene = OTHER_CORRECT_VARIANT_GENE,
                proteinEffect = ProteinEffect.UNKNOWN,
                canonicalImpact = proteinImpact(OTHER_CORRECT_PROTEIN_IMPACT)
            )
        )
        val record = TestMolecularFactory.createMinimalTestMolecularRecord().copy(
            drivers = Drivers(variants = variants, emptySet(), emptySet(), emptySet(), emptySet(), emptySet())
        )
        val evaluation = function.evaluate(
            TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
                molecularHistory = MolecularHistory(listOf(record))
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, evaluation)
        Assertions.assertThat(evaluation.passGeneralMessages).isEqualTo(
            setOf(
                "$CORRECT_PROTEIN_IMPACT detected in $CORRECT_VARIANT_GENE",
                "$OTHER_CORRECT_PROTEIN_IMPACT detected in $OTHER_CORRECT_VARIANT_GENE"
            )
        )
    }

    @Test
    fun `Should fail for unreported variant`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    BASE_VARIANT.copy(
                        isReportable = false
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass for correct exon skipping variant`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(MolecularTestFactory.withFusion(BASE_EXON_SKIPPING_FUSION))
        )
    }

    @Test
    fun `Should fail for incorrect exon skipping variant`() {
        val incorrectExonSkippingFusion =
            BASE_EXON_SKIPPING_FUSION.copy(
                extendedFusionDetails = TestFusionFactory.createMinimalExtended().copy(fusedExonUp = 1, fusedExonDown = 3)
            )
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withFusion(incorrectExonSkippingFusion))
        )
    }

    @Test
    fun `Should pass for correct fusion`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(MolecularTestFactory.withFusion(BASE_FUSION.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION)))
        )
    }

    @Test
    fun `Should warn for correct fusion gene but low driver likelihood`() {
        val fusions = BASE_FUSION.copy(driverLikelihood = DriverLikelihood.LOW)
        EvaluationAssert.assertEvaluation(EvaluationResult.WARN, function.evaluate(MolecularTestFactory.withFusion(fusions)))
    }

    @Test
    fun `Should fail for incorrect fusion`() {
        val fusions = BASE_FUSION.copy(
            geneStart = "Wrong gene",
            geneEnd = "Fusion partner",
            driverLikelihood = DriverLikelihood.HIGH
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withFusion(fusions)))
    }

    @Test
    fun `Should pass with deletion of correct gene and correct exon`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = CORRECT_DELETION_GENE, isReportable = true,
                        type = VariantType.DELETE,
                        canonicalImpact = impactWithExon(CORRECT_DELETION_CODON),
                        extendedVariantDetails = TestVariantFactory.createMinimalExtended()
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail with deletion of incorrect gene`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = "incorrect", isReportable = true,
                        type = VariantType.DELETE,
                        canonicalImpact = impactWithExon(CORRECT_DELETION_CODON)
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass with insertion of correct gene and correct exon`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = CORRECT_INSERTION_GENE, isReportable = true,
                        type = VariantType.INSERT,
                        canonicalImpact = impactWithExon(CORRECT_INSERTION_CODON),
                        extendedVariantDetails = TestVariantFactory.createMinimalExtended()
                    )
                )
            )
        )
    }

    @Test
    fun `Should fail with insertion of incorrect gene`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = "incorrect", isReportable = true,
                        type = VariantType.INSERT,
                        canonicalImpact = impactWithExon(CORRECT_INSERTION_CODON)
                    )
                )
            )
        )
    }

    private fun proteinImpact(hgvsProteinImpact: String): TranscriptImpact {
        return TestTranscriptImpactFactory.createMinimal().copy(hgvsProteinImpact = hgvsProteinImpact)
    }

    private fun impactWithExon(affectedExon: Int) = TestTranscriptImpactFactory.createMinimal().copy(affectedExon = affectedExon)

    companion object {

    }
}