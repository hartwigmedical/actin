package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasPSMAPositivePETScanTest {

    @Test
    public void canEvaluate() {
        HasPSMAPositivePETScan function = new HasPSMAPositivePETScan();

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}

