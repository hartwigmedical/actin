package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.molecular.datamodel.driver.GeneRole;
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect;
import com.hartwig.actin.molecular.datamodel.driver.TestAmplificationFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestDisruptionFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestFusionFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestHomozygousDisruptionFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestLossFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory;

import org.junit.Test;

public class GeneIsWildTypeTest {

    @Test
    public void canEvaluateVariants() {
        GeneIsWildType function = new GeneIsWildType("gene A");

        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        assertMolecularEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                        .build())));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .proteinEffect(ProteinEffect.NO_EFFECT)
                        .build())));
    }

    @Test
    public void canEvaluateAmplifications() {
        GeneIsWildType function = new GeneIsWildType("gene A");

        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withAmplification(TestAmplificationFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                        .geneRole(GeneRole.ONCO)
                        .build())));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withAmplification(TestAmplificationFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .proteinEffect(ProteinEffect.NO_EFFECT)
                        .geneRole(GeneRole.ONCO)
                        .build())));

        assertMolecularEvaluation(EvaluationResult.PASS,
                function.evaluate(MolecularTestFactory.withAmplification(TestAmplificationFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                        .geneRole(GeneRole.TSG)
                        .build())));
    }

    @Test
    public void canEvaluateLosses() {
        GeneIsWildType function = new GeneIsWildType("gene A");

        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withLoss(TestLossFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .proteinEffect(ProteinEffect.LOSS_OF_FUNCTION)
                        .geneRole(GeneRole.TSG)
                        .build())));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withLoss(TestLossFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .proteinEffect(ProteinEffect.NO_EFFECT)
                        .geneRole(GeneRole.TSG)
                        .build())));

        assertMolecularEvaluation(EvaluationResult.PASS,
                function.evaluate(MolecularTestFactory.withLoss(TestLossFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .proteinEffect(ProteinEffect.LOSS_OF_FUNCTION)
                        .geneRole(GeneRole.ONCO)
                        .build())));
    }

    @Test
    public void canEvaluateHomozygousDisruptions() {
        GeneIsWildType function = new GeneIsWildType("gene A");

        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        assertMolecularEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withHomozygousDisruption(TestHomozygousDisruptionFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .proteinEffect(ProteinEffect.LOSS_OF_FUNCTION)
                        .geneRole(GeneRole.TSG)
                        .build())));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withHomozygousDisruption(TestHomozygousDisruptionFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .proteinEffect(ProteinEffect.NO_EFFECT)
                        .geneRole(GeneRole.TSG)
                        .build())));

        assertMolecularEvaluation(EvaluationResult.PASS,
                function.evaluate(MolecularTestFactory.withHomozygousDisruption(TestHomozygousDisruptionFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .proteinEffect(ProteinEffect.LOSS_OF_FUNCTION)
                        .geneRole(GeneRole.ONCO)
                        .build())));
    }

    @Test
    public void canEvaluateDisruptions() {
        GeneIsWildType function = new GeneIsWildType("gene A");

        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        assertMolecularEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withDisruption(TestDisruptionFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .proteinEffect(ProteinEffect.LOSS_OF_FUNCTION)
                        .geneRole(GeneRole.TSG)
                        .build())));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withDisruption(TestDisruptionFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .proteinEffect(ProteinEffect.NO_EFFECT)
                        .geneRole(GeneRole.TSG)
                        .build())));

        assertMolecularEvaluation(EvaluationResult.PASS,
                function.evaluate(MolecularTestFactory.withDisruption(TestDisruptionFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .proteinEffect(ProteinEffect.LOSS_OF_FUNCTION)
                        .geneRole(GeneRole.ONCO)
                        .build())));
    }

    @Test
    public void canEvaluateFusions() {
        GeneIsWildType function = new GeneIsWildType("gene A");

        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        assertMolecularEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withFusion(TestFusionFactory.builder()
                        .geneStart("gene A")
                        .isReportable(true)
                        .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                        .build())));

        assertMolecularEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withFusion(TestFusionFactory.builder()
                        .geneEnd("gene A")
                        .isReportable(true)
                        .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                        .build())));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withFusion(TestFusionFactory.builder()
                        .geneStart("gene A")
                        .isReportable(true)
                        .proteinEffect(ProteinEffect.NO_EFFECT)
                        .build())));
    }
}