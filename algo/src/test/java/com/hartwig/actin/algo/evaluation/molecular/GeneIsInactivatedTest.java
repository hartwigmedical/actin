package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.molecular.datamodel.driver.GeneRole;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect;
import com.hartwig.actin.molecular.datamodel.driver.TestHomozygousDisruptionFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestLossFactory;

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

        assertMolecularEvaluation(EvaluationResult.PASS,
                function.evaluate(MolecularTestFactory.withLoss(matchingLoss)));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withLoss(TestLossFactory.builder()
                        .from(matchingLoss)
                        .isReportable(false)
                        .build())));

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
}