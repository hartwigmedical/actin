package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect
import com.hartwig.actin.molecular.datamodel.driver.TestFusionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestTranscriptImpactFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import com.hartwig.actin.molecular.datamodel.driver.TranscriptImpact
import com.hartwig.actin.molecular.datamodel.driver.VariantType
import org.assertj.core.api.Assertions
import org.junit.Test

class HasMolecularEventWithSocTargetedTherapyForNSCLCAvailableTest{

    private val function = HasMolecularEventWithSocTargetedTherapyForNSCLCAvailable(emptySet())

    @Test
    fun `Should fail when molecular record is empty`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestPatientFactory.createMinimalTestPatientRecord()))
    }

    @Test
    fun `Should pass for activating mutation in correct gene`() {
        val evaluation = function.evaluate(
            MolecularTestFactory.withVariant(
                TestVariantFactory.createMinimal().copy(
                    gene = CORRECT_GENE,
                    isReportable = true,
                    driverLikelihood = DriverLikelihood.HIGH,
                    proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
                    clonalLikelihood = 1.0
                )
            )
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, evaluation)
        Assertions.assertThat(evaluation.passGeneralMessages).containsExactly("$CORRECT_GENE activating mutation(s)")
    }

    @Test
    fun `Should fail if activating mutation is in correct gene but this gene is in geneToIgnore`(){
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL,
            HasMolecularEventWithSocTargetedTherapyForNSCLCAvailable(setOf(CORRECT_GENE)).evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = CORRECT_GENE,
                        isReportable = true,
                        driverLikelihood = DriverLikelihood.HIGH,
                        proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
                        clonalLikelihood = 1.0
                    )
                )
            )
        )
    }

    @Test
    fun `Should warn for mutation in correct gene when uncertain if activating`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.WARN,
            function.evaluate(MolecularTestFactory.withVariant(
                TestVariantFactory.createMinimal().copy(
                    gene = CORRECT_GENE,
                    isReportable = true,
                    driverLikelihood = DriverLikelihood.HIGH,
                    proteinEffect = ProteinEffect.UNKNOWN,
                    clonalLikelihood = 1.0
                )
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
                    TestVariantFactory.createMinimal().copy(
                        gene = "Wrong",
                        isReportable = true,
                        driverLikelihood = DriverLikelihood.HIGH,
                        proteinEffect = ProteinEffect.GAIN_OF_FUNCTION,
                        clonalLikelihood = 1.0
                    )
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
                    TestVariantFactory.createMinimal().copy(
                        gene = CORRECT_VARIANT_GENE,
                        isReportable = true,
                        clonalLikelihood = 1.0,
                        canonicalImpact = proteinImpact(CORRECT_PROTEIN_IMPACT)
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass for multiple correct variants and should display correct message`(){
        val variants = setOf(
            TestVariantFactory.createMinimal().copy(
                gene = CORRECT_VARIANT_GENE,
                isReportable = true,
                clonalLikelihood = 1.0,
                canonicalImpact = proteinImpact(CORRECT_PROTEIN_IMPACT)
            ),
            TestVariantFactory.createMinimal().copy(
                gene = OTHER_CORRECT_VARIANT_GENE,
                isReportable = true,
                clonalLikelihood = 1.0,
                canonicalImpact = proteinImpact(OTHER_CORRECT_PROTEIN_IMPACT)
            )
        )
        val record = TestMolecularFactory.createMinimalTestMolecularRecord().copy(
            drivers = MolecularDrivers(variants = variants, emptySet(), emptySet(), emptySet(), emptySet(), emptySet())
        )
        val evaluation = function.evaluate(TestPatientFactory.createMinimalTestPatientRecord().copy(
            molecularHistory = MolecularHistory.fromInputs(listOf(record), emptyList()))
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
    fun `Should fail for incorrect variant`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = CORRECT_VARIANT_GENE, clonalLikelihood = 1.0, canonicalImpact = proteinImpact("1ABC2")
                    )
                )
            )
        )
    }

    @Test
    fun `Should pass for correct exon skipping variant`() {
        val exonSkippingFusion = TestFusionFactory.createMinimal().copy(
            isReportable = true,
            geneStart = CORRECT_EXON_SKIPPING_GENE,
            fusedExonUp = CORRECT_EXON_SKIPPING_EXON.minus(1),
            geneEnd = CORRECT_EXON_SKIPPING_GENE,
            fusedExonDown = CORRECT_EXON_SKIPPING_EXON.plus(1)
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withFusion(exonSkippingFusion)))
    }

    @Test
    fun `Should fail for incorrect exon skipping variant`() {
        val exonSkippingFusion = TestFusionFactory.createMinimal().copy(
            isReportable = true,
            geneStart = CORRECT_EXON_SKIPPING_GENE,
            fusedExonUp = 1,
            geneEnd = CORRECT_EXON_SKIPPING_GENE,
            fusedExonDown = 3
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withFusion(exonSkippingFusion)))
    }

    @Test
    fun `Should pass for correct fusion`() {
        val fusions = TestFusionFactory.createMinimal().copy(
            isReportable = true,
            geneStart = CORRECT_FUSION_GENE,
            fusedExonUp = 5,
            geneEnd = "Fusion partner",
            fusedExonDown = 3,
            driverLikelihood = DriverLikelihood.HIGH
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withFusion(fusions)))
    }

    @Test
    fun `Should warn for correct fusion gene but low driver likelihood`() {
        val fusions = TestFusionFactory.createMinimal().copy(
            isReportable = true,
            geneStart = CORRECT_FUSION_GENE,
            fusedExonUp = 5,
            geneEnd = "Fusion partner",
            fusedExonDown = 3,
            driverLikelihood = DriverLikelihood.LOW
        )
        EvaluationAssert.assertEvaluation(EvaluationResult.WARN, function.evaluate(MolecularTestFactory.withFusion(fusions)))
    }

    @Test
    fun `Should fail for incorrect fusion`() {
        val fusions = TestFusionFactory.createMinimal().copy(
            isReportable = true,
            geneStart = "Wrong gene",
            fusedExonUp = 1,
            geneEnd = "Fusion partner",
            fusedExonDown = 3,
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
                        canonicalImpact = impactWithExon(CORRECT_DELETION_CODON)
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
                        canonicalImpact = impactWithExon(CORRECT_INSERTION_CODON)
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
        private val CORRECT_GENE = "EGFR"
        private val CORRECT_VARIANT_GENE = "EGFR"
        private val CORRECT_PROTEIN_IMPACT = "L858R"
        private val OTHER_CORRECT_VARIANT_GENE = "BRAF"
        private val OTHER_CORRECT_PROTEIN_IMPACT = "V600E"
        private val CORRECT_EXON_SKIPPING_GENE = "MET"
        private val CORRECT_EXON_SKIPPING_EXON = 14
        private val CORRECT_FUSION_GENE = "ALK"
        private val CORRECT_DELETION_GENE = "EGFR"
        private val CORRECT_DELETION_CODON = 19
        private val CORRECT_INSERTION_GENE = "EGFR"
        private val CORRECT_INSERTION_CODON = 20
    }
}