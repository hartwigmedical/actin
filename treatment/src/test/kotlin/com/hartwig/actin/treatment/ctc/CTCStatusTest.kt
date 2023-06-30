package com.hartwig.actin.treatment.ctc

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CTCStatusTest {
    @Test
    fun shouldBeAbleToDetectOpenStatus() {
        assertThat(CTCStatus.fromStatusString("Open")).isEqualTo(CTCStatus.OPEN)
        assertThat(CTCStatus.fromStatusString("open")).isEqualTo(CTCStatus.OPEN)
    }

    @Test
    fun shouldBeAbleToDetectClosedStatus() {
        assertThat(CTCStatus.fromStatusString("Gesloten")).isEqualTo(CTCStatus.CLOSED)
        assertThat(CTCStatus.fromStatusString("nog niet geopend")).isEqualTo(CTCStatus.CLOSED)
    }

    @Test
    fun shouldResolveToClosedWhenUnclear() {
        assertThat(CTCStatus.fromStatusString("This is not clear")).isEqualTo(CTCStatus.CLOSED)
    }
}