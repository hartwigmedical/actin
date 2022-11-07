package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.molecular.datamodel.driver.TestTranscriptImpactFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory;
import com.hartwig.actin.molecular.datamodel.driver.VariantType;
import com.hartwig.actin.treatment.input.datamodel.VariantTypeInput;

import org.junit.Test;

public class GeneHasVariantInExonRangeOfTypeTest {

    @Test
    public void canEvaluate() {
        GeneHasVariantInExonRangeOfType function = new GeneHasVariantInExonRangeOfType("gene A", 1, 2, VariantTypeInput.INSERT);
        GeneHasVariantInExonRangeOfType function2 = new GeneHasVariantInExonRangeOfType("gene A", 1, 2, VariantTypeInput.INDEL);
        GeneHasVariantInExonRangeOfType function3 = new GeneHasVariantInExonRangeOfType("gene A", 1, 2, null);

        // gene not present
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        // no exons configured
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder().gene("gene A").build())));

        // no variant type configured
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .canonicalImpact(TestTranscriptImpactFactory.builder().affectedExon(1).build())
                        .build())));

        // no variant type configured when input = null
        assertEvaluation(EvaluationResult.PASS,
                function3.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .canonicalImpact(TestTranscriptImpactFactory.builder().affectedExon(1).build())
                        .build())));

        // wrong exon range
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .canonicalImpact(TestTranscriptImpactFactory.builder().affectedExon(6).build())
                        .build())));

        // wrong variant type
        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .type(VariantType.MNV)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().affectedExon(6).build())
                        .build())));

        // wrong variant type
        assertEvaluation(EvaluationResult.FAIL,
                function2.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .type(VariantType.MNV)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().affectedExon(6).build())
                        .build())));

        // correct gene, correct exon, correct variant type, canonical
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .type(VariantType.INSERT)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().affectedExon(1).build())
                        .build())));

        // correct gene, correct exon, correct variant type, canonical
        assertEvaluation(EvaluationResult.PASS,
                function2.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .type(VariantType.INSERT)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().affectedExon(1).build())
                        .build())));

        // correct gene, correct exon, correct variant type, non-canonical only
        assertEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .type(VariantType.INSERT)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().affectedExon(6).build())
                        .addOtherImpacts(TestTranscriptImpactFactory.builder().affectedExon(6).build())
                        .addOtherImpacts(TestTranscriptImpactFactory.builder().affectedExon(1).build())
                        .build())));
    }
}