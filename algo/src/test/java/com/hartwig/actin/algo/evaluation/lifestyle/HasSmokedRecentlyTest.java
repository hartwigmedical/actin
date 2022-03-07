package com.hartwig.actin.algo.evaluation.lifestyle;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasSmokedRecentlyTest {

    @Test
    public void canEvaluate() {
        HasSmokedRecently function = new HasSmokedRecently();

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}