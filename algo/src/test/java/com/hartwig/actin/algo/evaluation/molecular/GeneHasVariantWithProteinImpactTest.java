package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.google.common.collect.Lists;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class GeneHasVariantWithProteinImpactTest {

    @Test
    public void canEvaluate() {
        GeneHasVariantWithProteinImpact function = new GeneHasVariantWithProteinImpact("gene A", Lists.newArrayList("V600E", "V600K"));

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}