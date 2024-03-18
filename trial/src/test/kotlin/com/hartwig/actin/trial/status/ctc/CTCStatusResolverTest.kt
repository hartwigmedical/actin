package com.hartwig.actin.trial.status.ctc

import com.hartwig.actin.trial.status.TrialStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class CTCStatusResolverTest {

    @Test
    fun `Should resolve open status`() {
        assertEquals(TrialStatus.OPEN, CTCStatusResolver.resolve("Open"))
    }

    @Test
    fun `Should resolve closed status`() {
        assertEquals(TrialStatus.CLOSED, CTCStatusResolver.resolve("Gesloten"))
        assertEquals(TrialStatus.CLOSED, CTCStatusResolver.resolve("Nog niet geopend"))
        assertEquals(TrialStatus.CLOSED, CTCStatusResolver.resolve("Gesloten voor inclusie"))
        assertEquals(TrialStatus.CLOSED, CTCStatusResolver.resolve("Onbekend"))
        assertEquals(TrialStatus.CLOSED, CTCStatusResolver.resolve("Tijdelijk gesloten"))
        assertEquals(TrialStatus.CLOSED, CTCStatusResolver.resolve("Closed"))
    }

}