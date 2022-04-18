package com.hartwig.actin.molecular.orange.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EventFormatterTest {

    @Test
    public void canFormatEvents() {
        assertEquals("V600E", EventFormatter.format("p.Val600Glu"));
        assertEquals("V600E", EventFormatter.format("V600E"));

        assertEquals("del", EventFormatter.format("partial loss"));

        assertEquals("EML4-ALK", EventFormatter.format("EML4 - ALK"));

        assertEquals("splice", EventFormatter.format("p.?"));

        assertEquals("p", EventFormatter.format("p.p.p"));
    }
}