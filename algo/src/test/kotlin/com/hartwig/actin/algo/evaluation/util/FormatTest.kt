package com.hartwig.actin.algo.evaluation.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.junit.Test;

public class FormatTest {

    @Test
    public void canConcatStrings() {
        assertTrue(Format.concat(Sets.newHashSet()).isEmpty());

        assertEquals("string", Format.concat(Sets.newHashSet("string")));

        assertEquals("string1; string2", Format.concat(Sets.newHashSet("string1", "string2")));
        assertEquals("string1", Format.concat(Lists.newArrayList("string1", "string1")));
    }

    @Test
    public void canFormatDates() {
        assertNotNull(Format.date(LocalDate.of(2021, 8, 20)));
    }

    @Test
    public void canFormatPercentages() {
        assertEquals("50%", Format.percentage(0.500002));
    }

    @Test (expected = IllegalArgumentException.class)
    public void crashOnIllegalPercentage() {
        Format.percentage(50D);
    }
}