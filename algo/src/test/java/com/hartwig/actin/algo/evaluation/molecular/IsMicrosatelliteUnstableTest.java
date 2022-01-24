package com.hartwig.actin.algo.evaluation.molecular;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.algo.datamodel.Evaluation;

import org.junit.Test;

public class IsMicrosatelliteUnstableTest {

    @Test
    public void canEvaluate() {
        IsMicrosatelliteUnstable function = new IsMicrosatelliteUnstable();

        assertEquals(Evaluation.UNDETERMINED, function.evaluate(MolecularTestFactory.withMicrosatelliteInstability(null)));
        assertEquals(Evaluation.PASS, function.evaluate(MolecularTestFactory.withMicrosatelliteInstability(true)));
        assertEquals(Evaluation.FAIL, function.evaluate(MolecularTestFactory.withMicrosatelliteInstability(false)));
    }
}