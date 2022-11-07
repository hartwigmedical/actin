package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

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
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        // correct gene, no codon configured
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder().gene("gene A").build())));

        // correct gene, correct codon on canonical transcript
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .canonicalImpact(TestTranscriptImpactFactory.builder().affectedCodon(100).build())
                        .build())));

        // correct gene, correct codon on other transcript
        assertEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .canonicalImpact(TestTranscriptImpactFactory.builder().affectedCodon(300).build())
                        .addOtherImpacts(TestTranscriptImpactFactory.builder().affectedCodon(300).build())
                        .addOtherImpacts(TestTranscriptImpactFactory.builder().affectedCodon(100).build())
                        .build())));

        // correct gene, wrong codon
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .canonicalImpact(TestTranscriptImpactFactory.builder().affectedCodon(300).build())
                        .build())));
    }
}