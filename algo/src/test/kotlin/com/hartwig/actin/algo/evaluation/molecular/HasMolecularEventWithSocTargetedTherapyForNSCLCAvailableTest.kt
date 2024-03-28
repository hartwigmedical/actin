package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert
import com.hartwig.actin.algo.evaluation.molecular.HasMolecularEventWithSocTargetedTherapyForNSCLCAvailable.Companion.NSCLC_SOC_TARGETABLE_VARIANTS
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect
import com.hartwig.actin.molecular.datamodel.driver.TestFusionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestTranscriptImpactFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import com.hartwig.actin.molecular.datamodel.driver.TranscriptImpact
import com.hartwig.actin.molecular.datamodel.driver.VariantType
import org.junit.Test

class HasMolecularEventWithSocTargetedTherapyForNSCLCAvailableTest{

    private val function = HasMolecularEventWithSocTargetedTherapyForNSCLCAvailable(emptyList())

    @Test
    fun `Should fail when molecular record is empty`() {
        EvaluationAssert.assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestPatientFactory.createMinimalTestPatientRecord()))
    }

    @Test
    fun `Should pass for activating mutation in correct gene`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
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
    fun `Should pass for correct variant with protein impact`() {
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
    fun `Should fail for incorrect variant`() {
        EvaluationAssert.assertEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = CORRECT_VARIANT_GENE,
                        isReportable = true,
                        clonalLikelihood = 1.0,
                        canonicalImpact = proteinImpact("1ABC2")
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
    fun `Should fail with deletion of incorrect gene or exon`() {
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
        EvaluationAssert.assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = CORRECT_DELETION_GENE, isReportable = true,
                        type = VariantType.DELETE,
                        canonicalImpact = impactWithExon(123)
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
    fun `Should fail with insertion of incorrect gene or exon`() {
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
        EvaluationAssert.assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.createMinimal().copy(
                        gene = CORRECT_INSERTION_GENE, isReportable = true,
                        type = VariantType.INSERT,
                        canonicalImpact = impactWithExon(123)
                    )
                )
            )
        )
    }

    private fun proteinImpact(hgvsProteinImpact: String): TranscriptImpact {
        return TestTranscriptImpactFactory.createMinimal().copy(hgvsProteinImpact = hgvsProteinImpact)
    }
    private fun impactWithExon(affectedExon: Int) = TestTranscriptImpactFactory.createMinimal().copy(affectedExon = affectedExon)

    private val CORRECT_GENE = NSCLC_SOC_TARGETABLE_VARIANTS["Activating variant in gene"]!!.first()
    private val CORRECT_VARIANT_GENE = NSCLC_SOC_TARGETABLE_VARIANTS["Variants with protein impact"]!!.first().split(" ").first()
    private val CORRECT_PROTEIN_IMPACT = NSCLC_SOC_TARGETABLE_VARIANTS["Variants with protein impact"]!!.first().split(" ").elementAt(1)
    private val CORRECT_EXON_SKIPPING_GENE = NSCLC_SOC_TARGETABLE_VARIANTS["Exon skipping"]!!.first().split(" ").first()
    private val CORRECT_EXON_SKIPPING_EXON = NSCLC_SOC_TARGETABLE_VARIANTS["Exon skipping"]!!.first().split(" ").elementAt(1).toInt()
    private val CORRECT_FUSION_GENE = NSCLC_SOC_TARGETABLE_VARIANTS["Fusions"]!!.first()
    private val CORRECT_DELETION_GENE = NSCLC_SOC_TARGETABLE_VARIANTS["Deletions"]!!.first().split(" ").first()
    private val CORRECT_DELETION_CODON = NSCLC_SOC_TARGETABLE_VARIANTS["Deletions"]!!.first().split(" ").elementAt(1).toInt()
    private val CORRECT_INSERTION_GENE = NSCLC_SOC_TARGETABLE_VARIANTS["Deletions"]!!.first().split(" ").first()
    private val CORRECT_INSERTION_CODON = NSCLC_SOC_TARGETABLE_VARIANTS["Insertions"]!!.first().split(" ").elementAt(1).toInt()
}