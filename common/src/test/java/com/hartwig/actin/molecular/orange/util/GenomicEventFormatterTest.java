package com.hartwig.actin.molecular.orange.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GenomicEventFormatterTest {

    @Test
    public void canFormatGenomicEvents() {
        assertEquals("V600E", GenomicEventFormatter.format("p.Val600Glu"));
        assertEquals("V600E", GenomicEventFormatter.format("V600E"));

        assertEquals("del", GenomicEventFormatter.format("partial loss"));

        assertEquals("p", GenomicEventFormatter.format("p.c.p.c.p"));
    }
}