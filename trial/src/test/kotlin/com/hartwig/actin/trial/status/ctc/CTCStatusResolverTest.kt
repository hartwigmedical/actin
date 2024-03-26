package com.hartwig.actin.trial.status.ctc

import com.hartwig.actin.trial.status.TrialStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CTCStatusResolverTest {

    @Test
    fun `Should resolve open status`() {
        assertThat(CTCStatusResolver.resolve("Open")).isEqualTo(TrialStatus.OPEN)
    }

    @Test
    fun `Should resolve closed status`() {
        assertThat(CTCStatusResolver.resolve("Gesloten")).isEqualTo(TrialStatus.CLOSED)
        assertThat(CTCStatusResolver.resolve("Nog niet geopend")).isEqualTo(TrialStatus.CLOSED)
        assertThat(CTCStatusResolver.resolve("Gesloten voor inclusie")).isEqualTo(TrialStatus.CLOSED)
        assertThat(CTCStatusResolver.resolve("Onbekend")).isEqualTo(TrialStatus.CLOSED)
        assertThat(CTCStatusResolver.resolve("Tijdelijk gesloten")).isEqualTo(TrialStatus.CLOSED)
        assertThat(CTCStatusResolver.resolve("Closed")).isEqualTo(TrialStatus.CLOSED)
    }

    @Test
    fun `Should resolve unexpected status to uninterpretable`() {
        assertThat(CTCStatusResolver.resolve("UNKNOWN")).isEqualTo(TrialStatus.UNINTERPRETABLE)
    }
}