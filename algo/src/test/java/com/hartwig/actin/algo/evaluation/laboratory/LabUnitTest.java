package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class LabUnitTest {

    @Test
    public void canResolveLabUnits() {
        assertNull(LabUnit.fromString("not a unit"));

        assertEquals(LabUnit.GRAM_PER_DECILITER, LabUnit.fromString(LabUnit.GRAM_PER_DECILITER.display()));
    }
}