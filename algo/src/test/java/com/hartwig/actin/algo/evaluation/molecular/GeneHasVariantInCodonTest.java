package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.google.common.collect.Lists;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class GeneHasVariantInCodonTest {

    @Test
    public void canEvaluate() {
        GeneHasVariantInCodon function = new GeneHasVariantInCodon("gene A", Lists.newArrayList("A100", "B200"));

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}