package com.hartwig.actin.molecular.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class GenomicEventFormatterTest {

    @Test
    public void canFormatGenomicEvents() {
        assertEquals("BRAF V600E", GenomicEventFormatter.format("BRAF p.Val600Glu"));
        assertEquals("BRAF V600E", GenomicEventFormatter.format("BRAF V600E"));

        assertEquals("PTEN del", GenomicEventFormatter.format("PTEN partial loss"));

        assertEquals("p", GenomicEventFormatter.format("p.c.p.c.p"));
    }
}