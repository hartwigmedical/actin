package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.google.common.collect.Lists;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.molecular.datamodel.driver.TestTranscriptImpactFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory;

import org.junit.Test;

public class GeneHasVariantWithCodingImpactTest {

    @Test
    public void canEvaluate() {
        GeneHasVariantWithProteinImpact function = new GeneHasVariantWithProteinImpact("gene A", Lists.newArrayList("c100", "c200"));

        // gene not present
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        // correct gene, no protein impacts configured
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder().gene("gene A").build())));

        // correct gene, only wrong protein impacts configured
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .canonicalImpact(TestTranscriptImpactFactory.builder().hgvsProteinImpact("c999").build())
                        .addOtherImpacts(TestTranscriptImpactFactory.builder().hgvsProteinImpact("c999").build())
                        .build())));

        // correct gene, protein impact detected in canonical
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .canonicalImpact(TestTranscriptImpactFactory.builder().hgvsProteinImpact("c100").build())
                        .build())));

        // incorrect gene, protein impact detected in canonical
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene B")
                        .canonicalImpact(TestTranscriptImpactFactory.builder().hgvsProteinImpact("c100").build())
                        .build())));

        // correct gene, protein impact detected in non-canonical only
        assertEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .canonicalImpact(TestTranscriptImpactFactory.builder().hgvsProteinImpact("c999").build())
                        .addOtherImpacts(TestTranscriptImpactFactory.builder().hgvsProteinImpact("c999").build())
                        .addOtherImpacts(TestTranscriptImpactFactory.builder().hgvsProteinImpact("c100").build())
                        .build())));
    }
}