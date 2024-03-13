package com.hartwig.actin.trial.status

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialStatusTest {

    @Test
    fun shouldBeAbleToDetectOpenStatus() {
        assertThat(TrialStatus.fromStatusString("Open")).isEqualTo(TrialStatus.OPEN)
        assertThat(TrialStatus.fromStatusString("open")).isEqualTo(TrialStatus.OPEN)
    }

    @Test
    fun shouldBeAbleToDetectClosedStatus() {
        assertThat(TrialStatus.fromStatusString("Gesloten")).isEqualTo(TrialStatus.CLOSED)
        assertThat(TrialStatus.fromStatusString("nog niet geopend")).isEqualTo(TrialStatus.CLOSED)
    }

    @Test
    fun shouldResolveToClosedWhenUnclear() {
        assertThat(TrialStatus.fromStatusString("This is not clear")).isEqualTo(TrialStatus.UNINTERPRETABLE)
    }
}