package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ExtractionUtilTest {

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canKeep3Digits() {
        assertEquals(3, ExtractionUtil.keep3Digits(3D), EPSILON);
        assertEquals(3.123, ExtractionUtil.keep3Digits(3.123), EPSILON);
        assertEquals(3.123, ExtractionUtil.keep3Digits(3.123456789), EPSILON);
    }
}