package com.hartwig.actin.trial.status.nki

import com.hartwig.actin.trial.status.TrialStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NKIStatusResolverTest {

    @Test
    fun `Should resolve open status`() {
        assertThat(NKIStatusResolver.resolve("OPEN")).isEqualTo(TrialStatus.OPEN)
    }

    @Test
    fun `Should resolve closed status`() {
        assertThat(NKIStatusResolver.resolve("CLOSED")).isEqualTo(TrialStatus.CLOSED)
        assertThat(NKIStatusResolver.resolve("PENDING")).isEqualTo(TrialStatus.CLOSED)
        assertThat(NKIStatusResolver.resolve("COMPLETED")).isEqualTo(TrialStatus.CLOSED)
        assertThat(NKIStatusResolver.resolve("WITHDRAWN")).isEqualTo(TrialStatus.CLOSED)

    }

    @Test
    fun `Should resolve unexpected status to uninterpretable`() {
        assertThat(NKIStatusResolver.resolve("UNKNOWN")).isEqualTo(TrialStatus.UNINTERPRETABLE)
    }
}