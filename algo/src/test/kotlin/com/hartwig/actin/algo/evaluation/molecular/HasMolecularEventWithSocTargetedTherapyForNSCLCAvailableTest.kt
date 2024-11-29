package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.Drivers
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.ProteinEffect
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.TranscriptImpact
import com.hartwig.actin.datamodel.molecular.VariantType
import com.hartwig.actin.datamodel.molecular.driver.TestFusionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import org.assertj.core.api.Assertions.assertThat
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
    fusedExonUp = 3,
    fusedExonDown = 5
)

private val BASE_EXON_SKIPPING_FUSION = BASE_FUSION.copy(
    geneStart = CORRECT_EXON_SKIPPING_GENE,
    geneEnd = CORRECT_EXON_SKIPPING_GENE,
    fusedExonUp = CORRECT_EXON_SKIPPING_EXON.minus(1),
    fusedExonDown = CORRECT_EXON_SKIPPING_EXON.plus(1)
)


class HasMolecularEventWithSocTargetedTherapyForNSCLCAvailableTest {

    private val functionIncludingAllGenes = HasMolecularEventWithSocTargetedTherapyForNSCLCAvailable(null, emptySet())
    private val functionIncludingSpecificGene = HasMolecularEventWithSocTargetedTherapyForNSCLCAvailable(setOf(CORRECT_GENE), emptySet())

    @Test
    fun `Should fail when molecular record is empty`() {
        evaluateFunctions(EvaluationResult.FAIL, TestPatientFactory.createMinimalTestWGSPatientRecord())
    }

    @Test
    fun `Should pass for activating mutation in correct gene`() {
        val record = MolecularTestFactory.withVariant(BASE_VARIANT)
        val expectedMessages = setOf("$CORRECT_GENE activating mutation(s)")
        evaluateFunctions(EvaluationResult.PASS, record)
        evaluateMessages(functionIncludingAllGenes.evaluate(record).passGeneralMessages, expectedMessages)
        evaluateMessages(functionIncludingSpecificGene.evaluate(record).passGeneralMessages, expectedMessages)
    }

    @Test
    fun `Should fail if activating mutation is in correct gene but this gene is in geneToIgnore`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            HasMolecularEventWithSocTargetedTherapyForNSCLCAvailable(null, setOf(CORRECT_GENE)).evaluate(
                MolecularTestFactory.withVariant(BASE_VARIANT)
            )
        )
    }

    @Test
    fun `Should warn for mutation in correct gene when uncertain if activating`() {
        evaluateFunctions(
            EvaluationResult.WARN, MolecularTestFactory.withVariant(
                BASE_VARIANT.copy(proteinEffect = ProteinEffect.UNKNOWN)
            )
        )
    }

    @Test
    fun `Should fail for activating mutation in wrong gene`() {
        val record = MolecularTestFactory.withVariant(BASE_VARIANT.copy(gene = "Wrong"))
        evaluateFunctions(EvaluationResult.FAIL, record)
    }

    @Test
    fun `Should pass for correct variant with correct protein impact`() {
        val record = MolecularTestFactory.withVariant(
            BASE_VARIANT.copy(proteinEffect = ProteinEffect.UNKNOWN, canonicalImpact = proteinImpact(CORRECT_PROTEIN_IMPACT))
        )
        evaluateFunctions(EvaluationResult.PASS, record)
    }

    @Test
    fun `Should pass for multiple correct variants and should display correct message`() {
        val variants = listOf(
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
        val record = TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
            molecularHistory = MolecularHistory(
                listOf(
                    TestMolecularFactory.createMinimalTestMolecularRecord().copy(
                        drivers = Drivers(variants = variants)
                    )
                )
            )
        )
        val expectedMessages = setOf(
            "$CORRECT_PROTEIN_IMPACT detected in $CORRECT_VARIANT_GENE",
            "$OTHER_CORRECT_PROTEIN_IMPACT detected in $OTHER_CORRECT_VARIANT_GENE"
        )
        evaluateFunctions(EvaluationResult.PASS, record)
        evaluateMessages(functionIncludingAllGenes.evaluate(record).passGeneralMessages, expectedMessages)
        evaluateMessages(functionIncludingSpecificGene.evaluate(record).passGeneralMessages, expectedMessages)
    }

    @Test
    fun `Should fail for unreported variant`() {
        val record = MolecularTestFactory.withVariant(BASE_VARIANT.copy(isReportable = false))
        evaluateFunctions(EvaluationResult.FAIL, record)
    }

    @Test
    fun `Should pass for correct exon skipping variant`() {
        val record = MolecularTestFactory.withFusion(BASE_EXON_SKIPPING_FUSION)
        evaluateFunctions(EvaluationResult.PASS, record)
    }

    @Test
    fun `Should fail for incorrect exon skipping variant`() {
        val incorrectExonSkippingFusion = BASE_EXON_SKIPPING_FUSION.copy(fusedExonUp = 1, fusedExonDown = 3)
        evaluateFunctions(EvaluationResult.FAIL, MolecularTestFactory.withFusion(incorrectExonSkippingFusion))
    }

    @Test
    fun `Should pass for correct fusion`() {
        evaluateFunctions(
            EvaluationResult.PASS, MolecularTestFactory.withFusion(BASE_FUSION.copy(proteinEffect = ProteinEffect.GAIN_OF_FUNCTION))
        )
    }

    @Test
    fun `Should warn for correct fusion gene but low driver likelihood`() {
        val fusions = BASE_FUSION.copy(driverLikelihood = DriverLikelihood.LOW)
        evaluateFunctions(EvaluationResult.WARN, MolecularTestFactory.withFusion(fusions))
    }

    @Test
    fun `Should fail for incorrect fusion`() {
        val fusions = BASE_FUSION.copy(
            geneStart = "Wrong gene",
            geneEnd = "Fusion partner",
            driverLikelihood = DriverLikelihood.HIGH
        )
        evaluateFunctions(EvaluationResult.FAIL, MolecularTestFactory.withFusion(fusions))
    }

    @Test
    fun `Should pass with deletion of correct gene and correct exon`() {
        val record = MolecularTestFactory.withVariant(
            TestVariantFactory.createMinimal().copy(
                gene = CORRECT_DELETION_GENE, isReportable = true,
                type = VariantType.DELETE,
                canonicalImpact = impactWithExon(CORRECT_DELETION_CODON),
                extendedVariantDetails = TestVariantFactory.createMinimalExtended()
            )
        )
        evaluateFunctions(EvaluationResult.PASS, record)
    }

    @Test
    fun `Should fail with deletion of incorrect gene`() {
        val record = MolecularTestFactory.withVariant(
            TestVariantFactory.createMinimal().copy(
                gene = "incorrect", isReportable = true,
                type = VariantType.DELETE,
                canonicalImpact = impactWithExon(CORRECT_DELETION_CODON)
            )
        )
        evaluateFunctions(EvaluationResult.FAIL, record)
    }

    @Test
    fun `Should pass with insertion of correct gene and correct exon`() {
        val record = MolecularTestFactory.withVariant(
            TestVariantFactory.createMinimal().copy(
                gene = CORRECT_INSERTION_GENE, isReportable = true,
                type = VariantType.INSERT,
                canonicalImpact = impactWithExon(CORRECT_INSERTION_CODON),
                extendedVariantDetails = TestVariantFactory.createMinimalExtended()
            )
        )
        evaluateFunctions(EvaluationResult.PASS, record)
    }

    @Test
    fun `Should fail with insertion of incorrect gene`() {
        val record = MolecularTestFactory.withVariant(
            TestVariantFactory.createMinimal().copy(
                gene = "incorrect", isReportable = true,
                type = VariantType.INSERT,
                canonicalImpact = impactWithExon(CORRECT_INSERTION_CODON)
            )
        )
        evaluateFunctions(EvaluationResult.FAIL, record)
    }

    private fun proteinImpact(hgvsProteinImpact: String): TranscriptImpact {
        return TestTranscriptImpactFactory.createMinimal().copy(hgvsProteinImpact = hgvsProteinImpact)
    }

    private fun impactWithExon(affectedExon: Int) = TestTranscriptImpactFactory.createMinimal().copy(affectedExon = affectedExon)

    private fun evaluateFunctions(expected: EvaluationResult, record: PatientRecord) {
        assertThat(functionIncludingAllGenes.evaluate(record).result).isEqualTo(expected)
        assertThat(functionIncludingSpecificGene.evaluate(record).result).isEqualTo(expected)
    }

    private fun evaluateMessages(expected: Set<String>, fromEvaluation: Set<String>) {
        assertThat(fromEvaluation).isEqualTo(expected)
    }
}