package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation;

import com.google.common.collect.Lists;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.molecular.datamodel.driver.TestTranscriptImpactFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory;

import org.junit.Test;

public class GeneHasVariantWithCodingImpactTest {

    @Test
    public void canEvaluate() {
        GeneHasVariantWithCodingImpact function = new GeneHasVariantWithCodingImpact("gene A", Lists.newArrayList("c100", "c200"));

        // gene not present
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        // correct gene, no coding impacts configured
        assertMolecularEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .build())));

        // correct gene, only wrong coding impacts configured
        assertMolecularEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().hgvsCodingImpact("c999").build())
                        .addOtherImpacts(TestTranscriptImpactFactory.builder().hgvsCodingImpact("c999").build())
                        .build())));

        // correct gene, coding impact detected in canonical
        assertMolecularEvaluation(EvaluationResult.PASS,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().hgvsCodingImpact("c100").build())
                        .build())));

        // correct gene, coding impact detected in canonical, but not reportable
        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .isReportable(false)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().hgvsCodingImpact("c100").build())
                        .build())));

        // incorrect gene, coding impact detected in canonical
        assertMolecularEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene B")
                        .isReportable(true)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().hgvsCodingImpact("c100").build())
                        .build())));

        // correct gene, coding impact detected in non-canonical only
        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().hgvsCodingImpact("c999").build())
                        .addOtherImpacts(TestTranscriptImpactFactory.builder().hgvsCodingImpact("c999").build())
                        .addOtherImpacts(TestTranscriptImpactFactory.builder().hgvsCodingImpact("c100").build())
                        .build())));
    }
}