package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class LabUnitTest {

    @Test
    public void canResolveLabUnits() {
        assertNull(LabUnit.fromString("not a unit"));
        assertEquals(LabUnit.G_PER_DL, LabUnit.fromString(LabUnit.G_PER_DL.display()));
    }
}