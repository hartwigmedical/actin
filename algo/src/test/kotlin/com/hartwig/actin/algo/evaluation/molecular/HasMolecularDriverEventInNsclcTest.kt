package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestFusionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptVariantImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.TranscriptVariantImpact
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val CORRECT_PROTEIN_IMPACT_GENE = "BRAF"
private const val CORRECT_PROTEIN_IMPACT = "V600E"
private const val CORRECT_ACTIVATING_MUTATION_GENE = "EGFR"
private const val CORRECT_ACTIVATING_MUTATION_PROTEIN_IMPACT = "L858R"
private const val CORRECT_EXON_SKIPPING_GENE = "MET"
private const val CORRECT_EXON_SKIPPING_EXON = 14
private const val CORRECT_FUSION_GENE = "ALK"

private val BASE_SPECIFIC_VARIANT = TestVariantFactory.createMinimal().copy(
    gene = CORRECT_PROTEIN_IMPACT_GENE,
    event = "$CORRECT_PROTEIN_IMPACT_GENE $CORRECT_PROTEIN_IMPACT",
    isReportable = true,
    driverLikelihood = DriverLikelihood.HIGH,
    clonalLikelihood = 1.0
)

val BASE_ACTIVATING_MUTATION = TestVariantFactory.createMinimal().copy(
    gene = CORRECT_ACTIVATING_MUTATION_GENE,
    event = "$CORRECT_ACTIVATING_MUTATION_GENE $CORRECT_ACTIVATING_MUTATION_PROTEIN_IMPACT",
    isCancerAssociatedVariant = true,
    isReportable = true,
    driverLikelihood = DriverLikelihood.HIGH,
    proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
    clonalLikelihood = 1.0
)

private val BASE_FUSION = TestFusionFactory.createMinimal().copy(
    isReportable = true,
    geneStart = "Partner gene",
    geneEnd = CORRECT_FUSION_GENE,
    driverLikelihood = DriverLikelihood.HIGH,
    fusedExonUp = 3,
    fusedExonDown = 5,
    proteinEffect = ProteinEffect.GAIN_OF_FUNCTION
)

private val BASE_EXON_SKIPPING_FUSION = BASE_FUSION.copy(
    geneStart = CORRECT_EXON_SKIPPING_GENE,
    geneEnd = CORRECT_EXON_SKIPPING_GENE,
    fusedExonUp = CORRECT_EXON_SKIPPING_EXON.minus(1),
    fusedExonDown = CORRECT_EXON_SKIPPING_EXON.plus(1)
)

class HasMolecularDriverEventInNsclcTest {

    private val functionIncludingAllGenes = createFunction(genesToInclude = null, genesToExclude = emptySet())
    private val functionIncludingSpecificGenes =
        createFunction(genesToInclude = setOf(CORRECT_ACTIVATING_MUTATION_GENE, CORRECT_PROTEIN_IMPACT_GENE), genesToExclude = emptySet())
    private val functionIncludingAtLeastGenes = createFunction(
        genesToInclude = setOf(CORRECT_ACTIVATING_MUTATION_GENE, CORRECT_PROTEIN_IMPACT_GENE),
        genesToExclude = emptySet(),
        warnForMatchesOutsideGenesToInclude = true
    )
    private val functionExcludingSpecificGenes =
        createFunction(genesToInclude = null, genesToExclude = setOf(CORRECT_ACTIVATING_MUTATION_GENE, CORRECT_PROTEIN_IMPACT_GENE))

    @Test
    fun `Should fail when molecular record is empty`() {
        evaluateAllFunctions(EvaluationResult.FAIL, TestPatientFactory.createMinimalTestWGSPatientRecord())
    }

    @Test
    fun `Should pass for activating mutation in correct gene with correct message`() {
        val record = MolecularTestFactory.withVariant(BASE_ACTIVATING_MUTATION)

        evaluateIncludeFunctions(EvaluationResult.PASS, record)
        evaluateIncludePassMessages(setOf("NSCLC driver event(s) detected: EGFR L858R"), record)
    }

    @Test
    fun `Should set correct message if withAvailableSoc is true`() {
        val record = MolecularTestFactory.withVariant(BASE_ACTIVATING_MUTATION)
        val function = createFunction(
            setOf(CORRECT_ACTIVATING_MUTATION_GENE, CORRECT_PROTEIN_IMPACT_GENE),
            emptySet(),
            withAvailableSOC = true
        )

        assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
        evaluateMessages(
            function.evaluate(record).passMessagesStrings(),
            setOf("NSCLC driver event(s) with available SOC detected: EGFR L858R")
        )
    }

    @Test
    fun `Should fail with activating mutation in gene if gene not in include list and warnForMatchesOutsideGenesToInclude is false`() {
        val record = MolecularTestFactory.withVariant(BASE_ACTIVATING_MUTATION)
        val function = createFunction(genesToInclude = setOf("ALK"), genesToExclude = emptySet())

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should fail with activating mutation in gene if gene is in exclude list`() {
        val record = MolecularTestFactory.withVariant(BASE_ACTIVATING_MUTATION)
        evaluateExcludeFunction(EvaluationResult.FAIL, record)
    }

    @Test
    fun `Should fail for activating mutation in gene that is never relevant as driver in NSCLC`() {
        val record = MolecularTestFactory.withVariant(BASE_SPECIFIC_VARIANT.copy(gene = "Wrong"))
        evaluateAllFunctions(EvaluationResult.FAIL, record)
    }

    @Test
    fun `Should warn for mutation in correct gene when this is outcome of evaluation`() {
        val record =
            MolecularTestFactory.withVariant(
                BASE_ACTIVATING_MUTATION.copy(
                    isCancerAssociatedVariant = false,
                    proteinEffect = ProteinEffect.UNKNOWN
                )
            )
        evaluateIncludeFunctions(EvaluationResult.WARN, record)
    }

    @Test
    fun `Should pass for correct variant with correct protein impact`() {
        val record = MolecularTestFactory.withVariant(BASE_SPECIFIC_VARIANT.copy(canonicalImpact = proteinImpact(CORRECT_PROTEIN_IMPACT)))
        evaluateIncludeFunctions(EvaluationResult.PASS, record)
    }

    @Test
    fun `Should fail for correct variant with correct protein impact but gene not in include list`() {
        val record = MolecularTestFactory.withVariant(BASE_SPECIFIC_VARIANT.copy(canonicalImpact = proteinImpact(CORRECT_PROTEIN_IMPACT)))
        val function = createFunction(setOf("ALK"), emptySet())
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record))
    }

    @Test
    fun `Should fail for correct variant with correct protein impact but gene in exclude list`() {
        val record = MolecularTestFactory.withVariant(BASE_SPECIFIC_VARIANT.copy(canonicalImpact = proteinImpact(CORRECT_PROTEIN_IMPACT)))
        evaluateExcludeFunction(EvaluationResult.FAIL, record)
    }

    @Test
    fun `Should fail for variant in correct gene but incorrect protein impact`() {
        val record = MolecularTestFactory.withVariant(BASE_SPECIFIC_VARIANT.copy(canonicalImpact = proteinImpact("W600W")))
        evaluateAllFunctions(EvaluationResult.FAIL, record)
    }

    @Test
    fun `Should fail for correct protein impact but incorrect gene`() {
        val record = MolecularTestFactory.withVariant(
            BASE_SPECIFIC_VARIANT.copy(
                gene = "wrong",
                canonicalImpact = proteinImpact(CORRECT_PROTEIN_IMPACT)
            )
        )
        evaluateAllFunctions(EvaluationResult.FAIL, record)
    }

    @Test
    fun `Should pass for multiple correct events and should display correct messages`() {
        val variants = listOf(BASE_SPECIFIC_VARIANT.copy(canonicalImpact = proteinImpact(CORRECT_PROTEIN_IMPACT)), BASE_ACTIVATING_MUTATION)
        val record = TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
            molecularTests = listOf(
                TestMolecularFactory.createMinimalWholeGenomeTest().copy(
                    drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = variants)
                )
            )
        )

        evaluateIncludeFunctions(EvaluationResult.PASS, record)
        evaluateIncludePassMessages(setOf("NSCLC driver event(s) detected: BRAF V600E and EGFR L858R"), record)
    }

    @Test
    fun `Should fail for multiple correct drivers if both are on exclude list`() {
        val variants = listOf(BASE_SPECIFIC_VARIANT.copy(canonicalImpact = proteinImpact(CORRECT_PROTEIN_IMPACT)), BASE_ACTIVATING_MUTATION)
        val record = TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
            molecularTests = listOf(
                TestMolecularFactory.createMinimalWholeGenomeTest().copy(
                    drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = variants)
                )
            )
        )
        evaluateExcludeFunction(EvaluationResult.FAIL, record)
    }

    @Test
    fun `Should pass for correct exon skipping variant`() {
        val record = MolecularTestFactory.withFusion(BASE_EXON_SKIPPING_FUSION)
        assertEvaluation(EvaluationResult.PASS, functionIncludingAllGenes.evaluate(record))
    }

    @Test
    fun `Should fail for incorrect exon skipping variant`() {
        val record = MolecularTestFactory.withFusion(BASE_EXON_SKIPPING_FUSION.copy(fusedExonUp = 1, fusedExonDown = 3))
        assertEvaluation(EvaluationResult.FAIL, functionIncludingAllGenes.evaluate(record))
    }

    @Test
    fun `Should pass for correct fusion`() {
        val record = MolecularTestFactory.withFusion(BASE_FUSION)
        assertEvaluation(EvaluationResult.PASS, functionIncludingAllGenes.evaluate(record))
    }

    @Test
    fun `Should warn for correct fusion gene but low driver likelihood`() {
        val record = MolecularTestFactory.withFusion(BASE_FUSION.copy(driverLikelihood = DriverLikelihood.LOW))
        assertEvaluation(EvaluationResult.WARN, functionIncludingAllGenes.evaluate(record))
    }

    @Test
    fun `Should fail for incorrect fusion and display correct message`() {
        val record = MolecularTestFactory.withFusion(BASE_FUSION.copy(geneEnd = "Fusion partner"))
        assertEvaluation(EvaluationResult.FAIL, functionIncludingAllGenes.evaluate(record))
        evaluateMessages(
            functionIncludingAllGenes.evaluate(record).failMessagesStrings(),
            setOf("No (applicable) NSCLC driver event(s) detected")
        )
    }

    @Test
    fun `Should warn for activating mutation in another gene if warnForMatchesOutsideGenesToInclude is true and gene is not in include list with correct message`() {
        val record = MolecularTestFactory.withVariant(BASE_ACTIVATING_MUTATION)
        val function = createFunction(setOf("ALK"), emptySet(), warnForMatchesOutsideGenesToInclude = true)

        assertEvaluation(EvaluationResult.WARN, function.evaluate(record))
        evaluateMessages(
            function.evaluate(record).warnMessagesStrings(),
            setOf("Potential NSCLC driver event(s) detected: EGFR L858R (but undetermined if applicable)")
        )
    }

    @Test
    fun `Should pass for activating mutation in correct gene if warnForMatchesOutsideGenesToInclude is true but gene is actually in include list`() {
        val record = MolecularTestFactory.withVariant(BASE_ACTIVATING_MUTATION)

        assertEvaluation(EvaluationResult.PASS, functionIncludingAtLeastGenes.evaluate(record))
        evaluateMessages(
            functionIncludingAtLeastGenes.evaluate(record).passMessagesStrings(),
            setOf("NSCLC driver event(s) detected: EGFR L858R")
        )
    }

    @Test
    fun `Should pass for activating mutation in correct gene if warnForMatchesOutsideGenesToInclude is true and there is one event in include list and one for which would be warned`() {
        val variants = listOf(BASE_SPECIFIC_VARIANT.copy(gene = "KRAS"), BASE_ACTIVATING_MUTATION)
        val record = TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
            molecularTests = listOf(
                TestMolecularFactory.createMinimalWholeGenomeTest().copy(
                    drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = variants)
                )
            )
        )

        assertEvaluation(EvaluationResult.PASS, functionIncludingAtLeastGenes.evaluate(record))
        evaluateMessages(
            functionIncludingAtLeastGenes.evaluate(record).passMessagesStrings(),
            setOf("NSCLC driver event(s) detected: EGFR L858R")
        )
    }

    private fun proteinImpact(hgvsProteinImpact: String): TranscriptVariantImpact {
        return TestTranscriptVariantImpactFactory.createMinimal().copy(hgvsProteinImpact = hgvsProteinImpact)
    }

    private fun evaluateIncludeFunctions(expected: EvaluationResult, record: PatientRecord) {
        assertEvaluation(expected, functionIncludingAllGenes.evaluate(record))
        assertEvaluation(expected, functionIncludingSpecificGenes.evaluate(record))
        assertEvaluation(expected, functionIncludingAtLeastGenes.evaluate(record))
    }

    private fun evaluateIncludePassMessages(expected: Set<String>, record: PatientRecord) {
        evaluateMessages(functionIncludingAllGenes.evaluate(record).passMessagesStrings(), expected)
        evaluateMessages(functionIncludingSpecificGenes.evaluate(record).passMessagesStrings(), expected)
        evaluateMessages(functionIncludingAtLeastGenes.evaluate(record).passMessagesStrings(), expected)
    }

    private fun evaluateExcludeFunction(expected: EvaluationResult, record: PatientRecord) {
        assertEvaluation(expected, functionExcludingSpecificGenes.evaluate(record))
    }

    private fun evaluateAllFunctions(expected: EvaluationResult, record: PatientRecord) {
        evaluateIncludeFunctions(expected, record)
        evaluateExcludeFunction(expected, record)
    }

    private fun evaluateMessages(fromEvaluation: Set<String>, expected: Set<String>) {
        assertThat(fromEvaluation).isEqualTo(expected)
    }

    private fun createFunction(
        genesToInclude: Set<String>?,
        genesToExclude: Set<String>,
        maxTestAge: LocalDate? = null,
        warnForMatchesOutsideGenesToInclude: Boolean = false,
        withAvailableSOC: Boolean = false
    ): HasMolecularDriverEventInNsclc {
        return HasMolecularDriverEventInNsclc(
            genesToInclude,
            genesToExclude,
            maxTestAge,
            warnForMatchesOutsideGenesToInclude,
            withAvailableSOC
        )
    }
}