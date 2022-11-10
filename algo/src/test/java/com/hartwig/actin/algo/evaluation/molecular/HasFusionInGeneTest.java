package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect;
import com.hartwig.actin.molecular.datamodel.driver.TestFusionFactory;

import org.junit.Test;

public class HasFusionInGeneTest {

    @Test
    public void canEvaluate() {
        HasFusionInGene function = new HasFusionInGene("gene A");

        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        Fusion matchingFusion = TestFusionFactory.builder()
                .geneStart("gene A")
                .isReportable(true)
                .driverLikelihood(DriverLikelihood.HIGH)
                .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                .build();

        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withFusion(matchingFusion)));

        assertMolecularEvaluation(EvaluationResult.PASS,
                function.evaluate(MolecularTestFactory.withFusion(TestFusionFactory.builder()
                        .from(matchingFusion)
                        .geneStart("gene B")
                        .geneEnd("gene A")
                        .build())));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withFusion(TestFusionFactory.builder()
                        .from(matchingFusion)
                        .isReportable(false)
                        .build())));

        assertMolecularEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withFusion(TestFusionFactory.builder()
                        .from(matchingFusion)
                        .isReportable(false)
                        .proteinEffect(ProteinEffect.NO_EFFECT)
                        .build())));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withFusion(TestFusionFactory.builder()
                        .from(matchingFusion)
                        .driverLikelihood(DriverLikelihood.LOW)
                        .build())));

        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withFusion(TestFusionFactory.builder()
                        .from(matchingFusion)
                        .proteinEffect(ProteinEffect.NO_EFFECT)
                        .build())));
    }
}