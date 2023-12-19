package com.hartwig.actin.molecular.filter;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AlwaysValidFilterTest {

    @Test
    public void isAlwaysValid() {
        AlwaysValidFilter filter = new AlwaysValidFilter();

        assertTrue(filter.include("not a gene"));
    }
}