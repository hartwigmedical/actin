package com.hartwig.actin.algo.evaluation.molecular;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class IsMicrosatelliteUnstableTest {

    @Test
    public void canEvaluate() {
        IsMicrosatelliteUnstable function = new IsMicrosatelliteUnstable();

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withMicrosatelliteInstability(null)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withMicrosatelliteInstability(true)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withMicrosatelliteInstability(false)));
    }
}