package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.ProteinEffect
import com.hartwig.actin.datamodel.molecular.driver.TestFusionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestTranscriptVariantImpactFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.TranscriptVariantImpact
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

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
    proteinEffect = ProteinEffect.UNKNOWN,
    extendedVariantDetails = TestVariantFactory.createMinimalExtended().copy(clonalLikelihood = 1.0)
)

val BASE_ACTIVATING_MUTATION = TestVariantFactory.createMinimal().copy(
    gene = CORRECT_ACTIVATING_MUTATION_GENE,
    event = "$CORRECT_ACTIVATING_MUTATION_GENE $CORRECT_ACTIVATING_MUTATION_PROTEIN_IMPACT",
    isHotspot = true,
    isReportable = true,
    driverLikelihood = DriverLikelihood.HIGH,
    proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
    extendedVariantDetails = TestVariantFactory.createMinimalExtended().copy(clonalLikelihood = 1.0)
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

class HasMolecularDriverEventInNSCLCTest {

    private val functionIncludingAllGenes = HasMolecularDriverEventInNSCLC(null, emptySet())
    private val functionIncludingSpecificGenes = HasMolecularDriverEventInNSCLC(setOf("EGFR", "BRAF"), emptySet())
    private val functionExcludingSpecificGenes = HasMolecularDriverEventInNSCLC(null, setOf("EGFR", "BRAF"))

    @Test
    fun `Should fail when molecular record is empty`() {
        evaluateIncludeFunctions(EvaluationResult.FAIL, TestPatientFactory.createMinimalTestWGSPatientRecord())
        evaluateExcludeFunction(EvaluationResult.FAIL, TestPatientFactory.createMinimalTestWGSPatientRecord())
    }

    @Test
    fun `Should pass for activating mutation in correct gene`() {
        val record = MolecularTestFactory.withVariant(BASE_ACTIVATING_MUTATION)
        val expectedMessages =
            setOf("$CORRECT_ACTIVATING_MUTATION_GENE activating mutation(s): $CORRECT_ACTIVATING_MUTATION_PROTEIN_IMPACT")

        evaluateIncludeFunctions(EvaluationResult.PASS, record)
        evaluateMessages(functionIncludingAllGenes.evaluate(record).passMessages, expectedMessages)
        evaluateMessages(functionIncludingSpecificGenes.evaluate(record).passMessages, expectedMessages)
    }

    @Test
    fun `Should fail if activating mutation is in gene but this gene is not in include list`() {
        val record = MolecularTestFactory.withVariant(BASE_ACTIVATING_MUTATION)
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            HasMolecularDriverEventInNSCLC(setOf("ALK"), emptySet()).evaluate(record)
        )
    }

    @Test
    fun `Should fail if activating mutation is in correct gene but this gene is in ignore list`() {
        val record = MolecularTestFactory.withVariant(BASE_ACTIVATING_MUTATION)
        evaluateExcludeFunction(EvaluationResult.FAIL, record)
    }

    @Test
    fun `Should fail for activating mutation in wrong gene`() {
        val record = MolecularTestFactory.withVariant(BASE_SPECIFIC_VARIANT.copy(gene = "Wrong"))
        evaluateAllFunctions(EvaluationResult.FAIL, record)
    }

    @Test
    fun `Should warn for mutation in correct gene when uncertain if activating`() {
        val record =
            MolecularTestFactory.withVariant(BASE_ACTIVATING_MUTATION.copy(isHotspot = false, proteinEffect = ProteinEffect.UNKNOWN))
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
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            HasMolecularDriverEventInNSCLC(setOf("ALK"), emptySet()).evaluate(record)
        )
    }

    @Test
    fun `Should fail for correct variant with correct protein impact but gene in ignore list`() {
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
                gene = "wrong", canonicalImpact = proteinImpact(
                    CORRECT_PROTEIN_IMPACT
                )
            )
        )
        evaluateAllFunctions(EvaluationResult.FAIL, record)
    }

    @Test
    fun `Should pass for multiple correct drivers and should display correct message`() {
        val variants = listOf(BASE_SPECIFIC_VARIANT.copy(canonicalImpact = proteinImpact(CORRECT_PROTEIN_IMPACT)), BASE_ACTIVATING_MUTATION)
        val record = TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
            molecularHistory = MolecularHistory(
                listOf(
                    TestMolecularFactory.createMinimalTestMolecularRecord().copy(
                        drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = variants)
                    )
                )
            )
        )
        val expectedMessages = setOf(
            "$CORRECT_ACTIVATING_MUTATION_GENE activating mutation(s): $CORRECT_ACTIVATING_MUTATION_PROTEIN_IMPACT",
            "$CORRECT_PROTEIN_IMPACT in $CORRECT_PROTEIN_IMPACT_GENE in canonical transcript"
        )

        evaluateIncludeFunctions(EvaluationResult.PASS, record)
        evaluateMessages(functionIncludingAllGenes.evaluate(record).passMessages, expectedMessages)
        evaluateMessages(functionIncludingSpecificGenes.evaluate(record).passMessages, expectedMessages)
        evaluateMessages(
            HasMolecularDriverEventInNSCLC(
                setOf(CORRECT_ACTIVATING_MUTATION_GENE),
                emptySet()
            ).evaluate(record).passMessages, setOf(expectedMessages.first())
        )
        evaluateMessages(
            HasMolecularDriverEventInNSCLC(null, setOf(CORRECT_ACTIVATING_MUTATION_GENE)).evaluate(
                record
            ).passMessages, setOf(expectedMessages.last())
        )
        evaluateMessages(
            HasMolecularDriverEventInNSCLC(
                setOf(CORRECT_PROTEIN_IMPACT_GENE),
                setOf(CORRECT_ACTIVATING_MUTATION_GENE)
            ).evaluate(
                record
            ).passMessages, setOf(expectedMessages.last())
        )
    }

    @Test
    fun `Should fail for multiple correct drivers if both are on ignore list`() {
        val variants = listOf(BASE_SPECIFIC_VARIANT.copy(canonicalImpact = proteinImpact(CORRECT_PROTEIN_IMPACT)), BASE_ACTIVATING_MUTATION)
        val record = TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
            molecularHistory = MolecularHistory(
                listOf(
                    TestMolecularFactory.createMinimalTestMolecularRecord().copy(
                        drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = variants)
                    )
                )
            )
        )
        evaluateExcludeFunction(EvaluationResult.FAIL, record)
    }

    @Test
    fun `Should pass for correct exon skipping variant`() {
        val record = MolecularTestFactory.withFusion(BASE_EXON_SKIPPING_FUSION)
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, functionIncludingAllGenes.evaluate(record))
    }

    @Test
    fun `Should fail for incorrect exon skipping variant`() {
        val record = MolecularTestFactory.withFusion(BASE_EXON_SKIPPING_FUSION.copy(fusedExonUp = 1, fusedExonDown = 3))
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, functionIncludingAllGenes.evaluate(record))
    }

    @Test
    fun `Should pass for correct fusion`() {
        val record = MolecularTestFactory.withFusion(BASE_FUSION)
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, functionIncludingAllGenes.evaluate(record))
    }

    @Test
    fun `Should warn for correct fusion gene but low driver likelihood`() {
        val record = MolecularTestFactory.withFusion(BASE_FUSION.copy(driverLikelihood = DriverLikelihood.LOW))
        EvaluationAssert.assertEvaluation(EvaluationResult.WARN, functionIncludingAllGenes.evaluate(record))
    }

    @Test
    fun `Should fail for incorrect fusion`() {
        val record = MolecularTestFactory.withFusion(
            BASE_FUSION.copy(
                geneStart = "Wrong gene",
                geneEnd = "Fusion partner",
                driverLikelihood = DriverLikelihood.HIGH
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, functionIncludingAllGenes.evaluate(record))
    }

    @Test
    fun `Should warn for activating mutation in another gene if at least is configured and gene is in include-at-least list`() {
        val record = MolecularTestFactory.withVariant(BASE_ACTIVATING_MUTATION)
        val function = HasMolecularDriverEventInNSCLC(setOf("ALK"), emptySet(), includeGenesAtLeast = true)

        EvaluationAssert.assertEvaluation(EvaluationResult.WARN, function.evaluate(record))
        evaluateMessages(
            function.evaluate(record).warnMessages,
            setOf("Undetermined if patient's molecular driver event is applicable as 'molecular driver event' in NSCLC")
        )
    }

    @Test
    fun `Should pass for activating mutation in correct gene if at least is configured and event in that gene is not in include-at-least list`() {
        val record = MolecularTestFactory.withVariant(BASE_ACTIVATING_MUTATION)
        val function = HasMolecularDriverEventInNSCLC(setOf(CORRECT_ACTIVATING_MUTATION_GENE), emptySet(), includeGenesAtLeast = true)

        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
        evaluateMessages(
            function.evaluate(record).passMessages, setOf("$CORRECT_ACTIVATING_MUTATION_GENE activating mutation(s): L858R")
        )
    }

    @Test
    fun `Should pass for activating mutation in correct gene if at least is configured and event one gene is and one gene is not in include-at-least list`() {
        val variants = listOf(BASE_SPECIFIC_VARIANT.copy(canonicalImpact = proteinImpact(CORRECT_PROTEIN_IMPACT)), BASE_ACTIVATING_MUTATION)
        val record = TestPatientFactory.createMinimalTestWGSPatientRecord().copy(
            molecularHistory = MolecularHistory(
                listOf(
                    TestMolecularFactory.createMinimalTestMolecularRecord().copy(
                        drivers = TestMolecularFactory.createMinimalTestDrivers().copy(variants = variants)
                    )
                )
            )
        )
        val function = HasMolecularDriverEventInNSCLC(setOf(CORRECT_ACTIVATING_MUTATION_GENE), emptySet(), includeGenesAtLeast = true)

        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(record))
    }

    private fun proteinImpact(hgvsProteinImpact: String): TranscriptVariantImpact {
        return TestTranscriptVariantImpactFactory.createMinimal().copy(hgvsProteinImpact = hgvsProteinImpact)
    }

    private fun evaluateIncludeFunctions(expected: EvaluationResult, record: PatientRecord) {
        EvaluationAssert.assertEvaluation(expected, functionIncludingAllGenes.evaluate(record))
        EvaluationAssert.assertEvaluation(expected, functionIncludingSpecificGenes.evaluate(record))
    }

    private fun evaluateExcludeFunction(expected: EvaluationResult, record: PatientRecord) {
        EvaluationAssert.assertEvaluation(expected, functionExcludingSpecificGenes.evaluate(record))
    }

    private fun evaluateAllFunctions(expected: EvaluationResult, record: PatientRecord) {
        EvaluationAssert.assertEvaluation(expected, functionIncludingAllGenes.evaluate(record))
        EvaluationAssert.assertEvaluation(expected, functionIncludingSpecificGenes.evaluate(record))
        EvaluationAssert.assertEvaluation(expected, functionExcludingSpecificGenes.evaluate(record))
    }

    private fun evaluateMessages(fromEvaluation: Set<String>, expected: Set<String>) {
        assertThat(fromEvaluation).isEqualTo(expected)
    }
}