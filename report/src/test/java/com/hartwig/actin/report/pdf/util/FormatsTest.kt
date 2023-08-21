package com.hartwig.actin.report.pdf.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FormatsTest {

    @Test
    public void canFormatNumbers() {
        assertEquals("2.12", Formats.twoDigitNumber(2.123));
        assertEquals("2.12", Formats.twoDigitNumber(2.12));
        assertEquals("2.1", Formats.twoDigitNumber(2.1));
        assertEquals("2", Formats.twoDigitNumber(2.0));
    }
}