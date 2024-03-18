package com.hartwig.actin.trial.status.nki

import com.hartwig.actin.trial.status.TrialStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class NKIStatusResolverTest {

    @Test
    fun `Should resolve open status`() {
        assertEquals(TrialStatus.OPEN, NKIStatusResolver.resolve("OPEN"))
    }

    @Test
    fun `Should resolve closed status`() {
        assertEquals(TrialStatus.CLOSED, NKIStatusResolver.resolve("CLOSED"))
        assertEquals(TrialStatus.CLOSED, NKIStatusResolver.resolve("PENDING"))
        assertEquals(TrialStatus.CLOSED, NKIStatusResolver.resolve("COMPLETED"))
        assertEquals(TrialStatus.CLOSED, NKIStatusResolver.resolve("WITHDRAWN"))
    }
}