package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation;

import com.google.common.collect.Lists;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.molecular.datamodel.driver.TestTranscriptImpactFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory;

import org.junit.Test;

public class GeneHasVariantInCodonTest {

    @Test
    public void canEvaluate() {
        GeneHasVariantInCodon function = new GeneHasVariantInCodon("gene A", Lists.newArrayList("A100", "B200"));

        // gene not present
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        // no codons configured
        assertMolecularEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .isReportable(true)
                        .gene("gene A")
                        .build())));

        // incorrect codon
        assertMolecularEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().affectedCodon(300).build())
                        .build())));

        // correct codon range
        assertMolecularEvaluation(EvaluationResult.PASS,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .clonalLikelihood(1)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().affectedCodon(100).build())
                        .build())));

        // correct codon range, but not reportable
        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .isReportable(false)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().affectedCodon(100).build())
                        .build())));

        // correct codon range, but subclonal
        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .clonalLikelihood(0.3)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().affectedCodon(100).build())
                        .build())));

        // correct codon range, but not on canonical transcript
        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().affectedCodon(300).build())
                        .addOtherImpacts(TestTranscriptImpactFactory.builder().affectedCodon(300).build())
                        .addOtherImpacts(TestTranscriptImpactFactory.builder().affectedCodon(100).build())
                        .build())));
    }
}