package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.molecular.datamodel.driver.CodingEffect;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.GeneRole;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect;
import com.hartwig.actin.molecular.datamodel.driver.TestDisruptionFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestHomozygousDisruptionFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestLossFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestTranscriptImpactFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory;
import com.hartwig.actin.molecular.datamodel.driver.Variant;

import org.junit.Test;

public class GeneIsInactivatedTest {

    @Test
    public void canEvaluateOnHomozygousDisruptions() {
        GeneIsInactivated function = new GeneIsInactivated("gene A");

        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        HomozygousDisruption matchingHomDisruption = TestHomozygousDisruptionFactory.builder()
                .gene("gene A")
                .isReportable(true)
                .geneRole(GeneRole.TSG)
                .proteinEffect(ProteinEffect.LOSS_OF_FUNCTION)
                .build();

        assertMolecularEvaluation(EvaluationResult.PASS,
                function.evaluate(MolecularTestFactory.withHomozygousDisruption(matchingHomDisruption)));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withHomozygousDisruption(TestHomozygousDisruptionFactory.builder()
                        .from(matchingHomDisruption)
                        .isReportable(false)
                        .build())));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withHomozygousDisruption(TestHomozygousDisruptionFactory.builder()
                        .from(matchingHomDisruption)
                        .geneRole(GeneRole.ONCO)
                        .build())));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withHomozygousDisruption(TestHomozygousDisruptionFactory.builder()
                        .from(matchingHomDisruption)
                        .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                        .build())));
    }

    @Test
    public void canEvaluateOnLosses() {
        GeneIsInactivated function = new GeneIsInactivated("gene A");

        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        Loss matchingLoss = TestLossFactory.builder()
                .gene("gene A")
                .isReportable(true)
                .geneRole(GeneRole.TSG)
                .proteinEffect(ProteinEffect.LOSS_OF_FUNCTION)
                .build();

        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withLoss(matchingLoss)));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withLoss(TestLossFactory.builder().from(matchingLoss).isReportable(false).build())));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withLoss(TestLossFactory.builder()
                        .from(matchingLoss)
                        .geneRole(GeneRole.ONCO)
                        .build())));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withLoss(TestLossFactory.builder()
                        .from(matchingLoss)
                        .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                        .build())));
    }

    @Test
    public void canEvaluateOnVariants() {
        GeneIsInactivated function = new GeneIsInactivated("gene A");

        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        Variant matchingVariant = TestVariantFactory.builder()
                .gene("gene A")
                .isReportable(true)
                .driverLikelihood(DriverLikelihood.HIGH)
                .isBiallelic(true)
                .clonalLikelihood(1)
                .geneRole(GeneRole.TSG)
                .proteinEffect(ProteinEffect.LOSS_OF_FUNCTION)
                .canonicalImpact(TestTranscriptImpactFactory.builder()
                        .codingEffect(GeneIsInactivated.INACTIVATING_CODING_EFFECTS.iterator().next())
                        .build())
                .build();

        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withVariant(matchingVariant)));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .from(matchingVariant)
                        .isReportable(false)
                        .build())));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .from(matchingVariant)
                        .geneRole(GeneRole.ONCO)
                        .build())));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .from(matchingVariant)
                        .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                        .build())));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .from(matchingVariant)
                        .driverLikelihood(DriverLikelihood.MEDIUM)
                        .build())));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .from(matchingVariant)
                        .isBiallelic(false)
                        .build())));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .from(matchingVariant)
                        .isBiallelic(true)
                        .clonalLikelihood(0.4)
                        .build())));

        assertMolecularEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .from(matchingVariant)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().codingEffect(CodingEffect.NONE).build())
                        .build())));
    }

    @Test
    public void canMergeMultipleUnphasedVariants() {
        GeneIsInactivated function = new GeneIsInactivated("gene A");
        Variant variantGroup1 = TestVariantFactory.builder()
                .gene("gene A")
                .isReportable(true)
                .canonicalImpact(TestTranscriptImpactFactory.builder().codingEffect(CodingEffect.NONSENSE_OR_FRAMESHIFT).build())
                .phaseGroup(1)
                .build();

        Variant variantGroup2 = TestVariantFactory.builder()
                .gene("gene A")
                .isReportable(true)
                .canonicalImpact(TestTranscriptImpactFactory.builder().codingEffect(CodingEffect.NONSENSE_OR_FRAMESHIFT).build())
                .phaseGroup(2)
                .build();

        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withVariant(variantGroup1)));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withVariants(variantGroup1, variantGroup2)));

        Disruption disruption = TestDisruptionFactory.builder().gene("gene A").isReportable(true).clusterGroup(1).build();

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withVariantAndDisruption(variantGroup1, disruption)));
    }
}