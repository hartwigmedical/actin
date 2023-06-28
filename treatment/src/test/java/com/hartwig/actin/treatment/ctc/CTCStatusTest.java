package com.hartwig.actin.treatment.ctc;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CTCStatusTest {

    @Test
    public void shouldBeAbleToDetectOpenStatus() {
        assertEquals(CTCStatus.OPEN, CTCStatus.fromStatusString("Open"));
        assertEquals(CTCStatus.OPEN, CTCStatus.fromStatusString("open"));
    }

    @Test
    public void shouldBeAbleToDetectClosedStatus() {
        assertEquals(CTCStatus.CLOSED, CTCStatus.fromStatusString("Gesloten"));
        assertEquals(CTCStatus.CLOSED, CTCStatus.fromStatusString("nog niet geopend"));
    }

    @Test
    public void shouldResolveToClosedWhenUnclear() {
        assertEquals(CTCStatus.CLOSED, CTCStatus.fromStatusString("This is not clear"));
    }
}