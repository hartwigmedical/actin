package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;
import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation;

import static org.junit.Assert.assertNotNull;

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

        // gene not present
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        // no exons configured
        assertMolecularEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .build())));

        // no variant type configured
        assertMolecularEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().affectedExon(1).build())
                        .build())));

        // wrong exon range
        assertMolecularEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().affectedExon(6).build())
                        .build())));

        // wrong variant type
        assertMolecularEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .type(VariantType.MNV)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().affectedExon(1).build())
                        .build())));

        // correct gene, correct exon, correct variant type, canonical
        assertMolecularEvaluation(EvaluationResult.PASS,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .type(VariantType.INSERT)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().affectedExon(1).build())
                        .build())));

        // correct gene, correct exon, correct variant type, canonical, but not reportable
        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .isReportable(false)
                        .type(VariantType.INSERT)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().affectedExon(1).build())
                        .build())));

        // correct gene, correct exon, correct variant type, non-canonical only
        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .type(VariantType.INSERT)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().affectedExon(6).build())
                        .addOtherImpacts(TestTranscriptImpactFactory.builder().affectedExon(6).build())
                        .addOtherImpacts(TestTranscriptImpactFactory.builder().affectedExon(1).build())
                        .build())));
    }

    @Test
    public void canEvaluateForINDELs() {
        GeneHasVariantInExonRangeOfType function = new GeneHasVariantInExonRangeOfType("gene A", 1, 2, VariantTypeInput.INDEL);

        assertMolecularEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .type(VariantType.MNV)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().affectedExon(1).build())
                        .build())));

        assertMolecularEvaluation(EvaluationResult.PASS,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .type(VariantType.INSERT)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().affectedExon(1).build())
                        .build())));
    }

    @Test
    public void canEvaluateForAllVariantInputTypes() {
        for (VariantTypeInput input : VariantTypeInput.values()) {
            GeneHasVariantInExonRangeOfType function = new GeneHasVariantInExonRangeOfType("gene A", 1, 2, input);
            assertNotNull(function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
        }
    }

    @Test
    public void canEvaluateWithoutVariantTypes() {
        GeneHasVariantInExonRangeOfType function = new GeneHasVariantInExonRangeOfType("gene A", 1, 2, null);

        assertMolecularEvaluation(EvaluationResult.PASS,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .isReportable(true)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().affectedExon(1).build())
                        .build())));
    }
}