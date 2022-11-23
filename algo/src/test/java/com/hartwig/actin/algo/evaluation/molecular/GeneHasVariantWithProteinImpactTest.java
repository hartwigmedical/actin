package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation;

import com.google.common.collect.Lists;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.molecular.datamodel.driver.TestTranscriptImpactFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory;

import org.junit.Test;

public class GeneHasVariantWithProteinImpactTest {

    @Test
    public void canEvaluate() {
        GeneHasVariantWithProteinImpact function = new GeneHasVariantWithProteinImpact("gene A", Lists.newArrayList("V600E", "V600K"));

        // gene not present
        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        // correct gene, no protein impacts configured
        assertMolecularEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .build())));

        // correct gene, only wrong protein impacts configured
        assertMolecularEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().hgvsProteinImpact("V600P").build())
                        .addOtherImpacts(TestTranscriptImpactFactory.builder().hgvsProteinImpact("V600P").build())
                        .build())));

        // incorrect gene, protein impact detected in canonical
        assertMolecularEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene B")
                        .isReportable(true)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().hgvsProteinImpact("V600E").build())
                        .build())));

        // correct gene, protein impact detected in canonical
        assertMolecularEvaluation(EvaluationResult.PASS,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .clonalLikelihood(1)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().hgvsProteinImpact("V600E").build())
                        .build())));

        // correct gene, protein impact detected in canonical, but non-reportable
        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .isReportable(false)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().hgvsProteinImpact("V600E").build())
                        .build())));

        // correct gene, protein impact detected in canonical, but non-reportable
        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .clonalLikelihood(0.3)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().hgvsProteinImpact("V600E").build())
                        .build())));

        // correct gene, protein impact detected in non-canonical only
        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().hgvsProteinImpact("V600P").build())
                        .addOtherImpacts(TestTranscriptImpactFactory.builder().hgvsProteinImpact("V600P").build())
                        .addOtherImpacts(TestTranscriptImpactFactory.builder().hgvsProteinImpact("V600E").build())
                        .build())));
    }
}