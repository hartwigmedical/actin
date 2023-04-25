package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class GeneIsNotExpressedTest {

    @Test
    public void canEvaluate() {
        GeneIsNotExpressed function = new GeneIsNotExpressed();

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}