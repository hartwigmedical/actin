package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.google.common.collect.Lists;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class GeneHasVariantWithCodingImpactTest {

    @Test
    public void canEvaluate() {
        GeneHasVariantWithCodingImpact function = new GeneHasVariantWithCodingImpact("gene A", Lists.newArrayList("TODO1", "TODO2"));

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}