package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation
import com.hartwig.actin.molecular.datamodel.driver.*
import org.junit.Test

class GeneIsInactivatedTest {

    @Test
    fun canEvaluateOnHomozygousDisruptions() {
        val function = GeneIsInactivated("gene A")
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()))

        val matchingHomDisruption: HomozygousDisruption = TestHomozygousDisruptionFactory.builder()
            .gene("gene A")
            .isReportable(true)
            .geneRole(GeneRole.TSG)
            .proteinEffect(ProteinEffect.LOSS_OF_FUNCTION)
            .build()
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(MolecularTestFactory.withHomozygousDisruption(matchingHomDisruption))
        )

        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomozygousDisruption(
                    TestHomozygousDisruptionFactory.builder()
                        .from(matchingHomDisruption)
                        .isReportable(false)
                        .build()
                )
            )
        )

        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomozygousDisruption(
                    TestHomozygousDisruptionFactory.builder()
                        .from(matchingHomDisruption)
                        .geneRole(GeneRole.ONCO)
                        .build()
                )
            )
        )

        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHomozygousDisruption(
                    TestHomozygousDisruptionFactory.builder()
                        .from(matchingHomDisruption)
                        .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                        .build()
                )
            )
        )
    }

    @Test
    fun canEvaluateOnLosses() {
        val function = GeneIsInactivated("gene A")
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()))

        val matchingLoss: CopyNumber = TestCopyNumberFactory.builder()
            .gene("gene A")
            .isReportable(true)
            .geneRole(GeneRole.TSG)
            .proteinEffect(ProteinEffect.LOSS_OF_FUNCTION)
            .type(CopyNumberType.LOSS)
            .build()
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withCopyNumber(matchingLoss)))

        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withCopyNumber(
                    TestCopyNumberFactory.builder()
                        .from(matchingLoss)
                        .isReportable(false)
                        .build()
                )
            )
        )

        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withCopyNumber(
                    TestCopyNumberFactory.builder()
                        .from(matchingLoss)
                        .geneRole(GeneRole.ONCO)
                        .build()
                )
            )
        )

        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withCopyNumber(
                    TestCopyNumberFactory.builder()
                        .from(matchingLoss)
                        .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                        .build()
                )
            )
        )
    }

    @Test
    fun canEvaluateOnVariants() {
        val function = GeneIsInactivated("gene A")
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()))

        val matchingVariant: Variant = TestVariantFactory.builder()
            .gene("gene A")
            .isReportable(true)
            .driverLikelihood(DriverLikelihood.HIGH)
            .isBiallelic(true)
            .clonalLikelihood(1.0)
            .geneRole(GeneRole.TSG)
            .proteinEffect(ProteinEffect.LOSS_OF_FUNCTION)
            .canonicalImpact(
                TestTranscriptImpactFactory.builder()
                    .codingEffect(GeneIsInactivated.INACTIVATING_CODING_EFFECTS.iterator().next())
                    .build()
            )
            .build()
        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withVariant(matchingVariant)))

        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.builder()
                        .from(matchingVariant)
                        .isReportable(false)
                        .build()
                )
            )
        )

        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.builder()
                        .from(matchingVariant)
                        .geneRole(GeneRole.ONCO)
                        .build()
                )
            )
        )

        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.builder()
                        .from(matchingVariant)
                        .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                        .build()
                )
            )
        )

        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.builder()
                        .from(matchingVariant)
                        .driverLikelihood(DriverLikelihood.MEDIUM)
                        .build()
                )
            )
        )

        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.builder()
                        .from(matchingVariant)
                        .isBiallelic(false)
                        .build()
                )
            )
        )

        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.builder()
                        .from(matchingVariant)
                        .isBiallelic(true)
                        .clonalLikelihood(0.4)
                        .build()
                )
            )
        )

        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withVariant(
                    TestVariantFactory.builder()
                        .from(matchingVariant)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().codingEffect(CodingEffect.NONE).build())
                        .build()
                )
            )
        )

        // high TML and high driver likelihood variant
        assertMolecularEvaluation(
            EvaluationResult.PASS,
            function.evaluate(
                MolecularTestFactory.withHasTumorMutationalLoadAndVariant(
                    true,
                    TestVariantFactory.builder()
                        .from(matchingVariant)
                        .build()
                )
            )
        )

        // high TML and low driver likelihood variant
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(
                MolecularTestFactory.withHasTumorMutationalLoadAndVariant(
                    true,
                    TestVariantFactory.builder()
                        .from(matchingVariant)
                        .driverLikelihood(DriverLikelihood.LOW)
                        .build()
                )
            )
        )

        // low TML and low driver non-biallelic likelihood variant
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHasTumorMutationalLoadAndVariant(
                    false,
                    TestVariantFactory.builder()
                        .from(matchingVariant)
                        .driverLikelihood(DriverLikelihood.LOW)
                        .isBiallelic(false)
                        .build()
                )
            )
        )

        // low TML and low driver biallelic likelihood variant
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(
                MolecularTestFactory.withHasTumorMutationalLoadAndVariant(
                    false,
                    TestVariantFactory.builder()
                        .from(matchingVariant)
                        .driverLikelihood(DriverLikelihood.LOW)
                        .isBiallelic(true)
                        .build()
                )
            )
        )
    }

    @Test
    fun canMergeMultipleUnphasedVariants() {
        val function = GeneIsInactivated("gene A")
        val variantGroup1: Variant = TestVariantFactory.builder()
            .gene("gene A")
            .isReportable(true)
            .canonicalImpact(TestTranscriptImpactFactory.builder().codingEffect(CodingEffect.NONSENSE_OR_FRAMESHIFT).build())
            .addPhaseGroups(1)
            .build()
        val variantGroup2: Variant = TestVariantFactory.builder()
            .gene("gene A")
            .isReportable(true)
            .canonicalImpact(TestTranscriptImpactFactory.builder().codingEffect(CodingEffect.NONSENSE_OR_FRAMESHIFT).build())
            .addPhaseGroups(2)
            .build()
        assertMolecularEvaluation(
            EvaluationResult.FAIL,
            function.evaluate(MolecularTestFactory.withHasTumorMutationalLoadAndVariant(true, variantGroup1))
        )
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(MolecularTestFactory.withHasTumorMutationalLoadAndVariants(true, variantGroup1, variantGroup2))
        )
        val disruption: Disruption = TestDisruptionFactory.builder().gene("gene A").isReportable(true).clusterGroup(1).build()
        assertMolecularEvaluation(
            EvaluationResult.WARN,
            function.evaluate(MolecularTestFactory.withHasTumorMutationalLoadAndVariantAndDisruption(true, variantGroup1, disruption))
        )
    }
}