package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.molecular.datamodel.driver.CodingContext;
import com.hartwig.actin.molecular.datamodel.driver.Effect;
import com.hartwig.actin.molecular.datamodel.driver.RegionType;
import com.hartwig.actin.molecular.datamodel.driver.TestDisruptionFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestTranscriptImpactFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory;

import org.junit.Test;

public class GeneHasUTR3LossTest {

    @Test
    public void canEvaluate() {
        GeneHasUTR3Loss function = new GeneHasUTR3Loss("gene A");

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withDisruption(TestDisruptionFactory.builder().gene("gene A").build())));
        assertEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withDisruption(TestDisruptionFactory.builder()
                        .gene("gene A")
                        .regionType(RegionType.EXONIC)
                        .codingContext(CodingContext.UTR_3P)
                        .build())));

        assertEvaluation(EvaluationResult.FAIL,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder().gene("gene A").build())));
        assertEvaluation(EvaluationResult.WARN,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .isHotspot(false)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().addEffects(Effect.THREE_PRIME_UTR).build())
                        .build())));
        assertEvaluation(EvaluationResult.PASS,
                function.evaluate(MolecularTestFactory.withVariant(TestVariantFactory.builder()
                        .gene("gene A")
                        .isHotspot(true)
                        .canonicalImpact(TestTranscriptImpactFactory.builder().addEffects(Effect.THREE_PRIME_UTR).build())
                        .build())));
    }
}