package com.hartwig.actin.algo.evaluation.tumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasSuperScanBoneScanTest {

    @Test
    public void canEvaluate() {
        HasSuperScanBoneScan function = new HasSuperScanBoneScan();

        assertEvaluation(EvaluationResult.NOT_EVALUATED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }
}