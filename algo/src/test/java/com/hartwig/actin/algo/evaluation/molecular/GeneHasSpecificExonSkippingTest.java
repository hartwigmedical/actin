package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertMolecularEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.molecular.datamodel.driver.CodingEffect;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.TestFusionFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestTranscriptImpactFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory;
import com.hartwig.actin.molecular.datamodel.driver.Variant;

import org.junit.Test;

public class GeneHasSpecificExonSkippingTest {

    @Test
    public void canEvaluate() {
        GeneHasSpecificExonSkipping function = new GeneHasSpecificExonSkipping("gene A", 2);

        assertMolecularEvaluation(EvaluationResult.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        Variant spliceVariant = TestVariantFactory.builder()
                .gene("gene A")
                .isReportable(true)
                .canonicalImpact(TestTranscriptImpactFactory.builder().affectedExon(2).isSpliceRegion(true).build())
                .build();

        assertMolecularEvaluation(EvaluationResult.WARN, function.evaluate(MolecularTestFactory.withVariant(spliceVariant)));
        assertMolecularEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .from(spliceVariant)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().affectedExon(2).codingEffect(CodingEffect.SPLICE).build())
                        .build())));

        assertMolecularEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .from(spliceVariant)
                        .isReportable(false)
                        .build())));

        Fusion exonSkippingFusion = TestFusionFactory.builder()
                .isReportable(true)
                .geneStart("gene A")
                .fusedExonUp(1)
                .geneEnd("gene A")
                .fusedExonDown(3)
                .build();

        assertMolecularEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withFusion(exonSkippingFusion)));
        assertMolecularEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withFusion(TestFusionFactory.builder()
                        .from(exonSkippingFusion)
                        .isReportable(false)
                        .build())));

        assertMolecularEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withFusion(TestFusionFactory.builder()
                        .from(exonSkippingFusion)
                        .fusedExonDown(5)
                        .build())));
    }
}