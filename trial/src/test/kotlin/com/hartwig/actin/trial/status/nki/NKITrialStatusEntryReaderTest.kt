package com.hartwig.actin.trial.status.nki

import com.google.common.io.Resources
import com.hartwig.actin.trial.status.TrialStatus
import com.hartwig.actin.trial.status.TrialStatusEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NKITrialStatusEntryReaderTest {

    @Test
    fun `Should read all trial status from JSON`() {
        val reader = NKITrialStatusEntryReader()
        val status = reader.read(Resources.getResource("nki_config").path)
        assertThat(status).containsExactly(
            TrialStatusEntry(
                studyId = 1,
                studyMETC = "MEC-001",
                studyAcronym = "OPN-001",
                studyTitle = "Open trial",
                studyStatus = TrialStatus.OPEN
            ),
            TrialStatusEntry(
                studyId = 2,
                studyMETC = "MEC-002",
                studyAcronym = "CLS-001",
                studyTitle = "Closed trial",
                studyStatus = TrialStatus.CLOSED
            ),
        )
    }
}