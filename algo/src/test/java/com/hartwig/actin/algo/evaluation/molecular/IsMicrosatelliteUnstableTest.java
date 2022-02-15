package com.hartwig.actin.algo.evaluation.molecular;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class IsMicrosatelliteUnstableTest {

    @Test
    public void canEvaluate() {
        IsMicrosatelliteUnstable function = new IsMicrosatelliteUnstable();

        assertEquals(EvaluationResult.UNDETERMINED, function.evaluate(MolecularTestFactory.withMicrosatelliteInstability(null)).result());
        assertEquals(EvaluationResult.PASS, function.evaluate(MolecularTestFactory.withMicrosatelliteInstability(true)).result());
        assertEquals(EvaluationResult.FAIL, function.evaluate(MolecularTestFactory.withMicrosatelliteInstability(false)).result());
    }
}