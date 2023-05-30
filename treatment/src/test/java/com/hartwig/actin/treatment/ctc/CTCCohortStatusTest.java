package com.hartwig.actin.treatment.ctc;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CTCCohortStatusTest {

    @Test
    public void shouldBeAbleToDetectOpenStatus() {
        assertEquals(CTCCohortStatus.OPEN, CTCCohortStatus.fromCohortStatusString("Open"));
        assertEquals(CTCCohortStatus.OPEN, CTCCohortStatus.fromCohortStatusString("open"));
    }

    @Test
    public void shouldBeAbleToDetectClosedStatus() {
        assertEquals(CTCCohortStatus.CLOSED, CTCCohortStatus.fromCohortStatusString("Gesloten"));
        assertEquals(CTCCohortStatus.CLOSED, CTCCohortStatus.fromCohortStatusString("nog niet geopend"));
    }

    @Test
    public void shouldResolveToClosedWhenUnclear() {
        assertEquals(CTCCohortStatus.CLOSED, CTCCohortStatus.fromCohortStatusString("This is not clear"));
    }
}