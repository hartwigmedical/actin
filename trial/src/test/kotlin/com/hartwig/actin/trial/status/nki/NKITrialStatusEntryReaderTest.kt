package com.hartwig.actin.trial.status.nki

import com.hartwig.actin.testutil.ResourceLocator.resourceOnClasspath
import com.hartwig.actin.trial.status.TrialStatus
import com.hartwig.actin.trial.status.TrialStatusEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NKITrialStatusEntryReaderTest {

    @Test
    fun `Should read all trial status from JSON and only include open, closed and suspended trials but always set slots available to 1`() {
        val status = NKITrialStatusEntryReader().read(resourceOnClasspath("nki_config"))
        assertThat(status).containsExactly(
            TrialStatusEntry(
                metcStudyID = "MEC-001",
                studyStatus = TrialStatus.OPEN,
                cohortId = "abcd",
                cohortStatus = TrialStatus.OPEN,
                cohortSlotsNumberAvailable = 1
            ),
            TrialStatusEntry(
                metcStudyID = "MEC-001",
                studyStatus = TrialStatus.OPEN,
                cohortId = "bcde",
                cohortStatus = TrialStatus.OPEN,
                cohortSlotsNumberAvailable = 1
            ),
            TrialStatusEntry(
                metcStudyID = "MEC-001",
                studyStatus = TrialStatus.OPEN,
                cohortId = "bcdef",
                cohortStatus = TrialStatus.CLOSED,
                cohortSlotsNumberAvailable = 1
            ), TrialStatusEntry(
                metcStudyID = "MEC-001",
                studyStatus = TrialStatus.OPEN,
                cohortId = "bcdeg",
                cohortStatus = TrialStatus.CLOSED,
                cohortSlotsNumberAvailable = 1
            ), TrialStatusEntry(
                metcStudyID = "MEC-002",
                studyStatus = TrialStatus.CLOSED,
                cohortId = "cdef",
                cohortStatus = TrialStatus.OPEN,
                cohortSlotsNumberAvailable = 1
            ),
            TrialStatusEntry(
                metcStudyID = "MEC-005",
                studyStatus = TrialStatus.CLOSED,
                cohortId = "ghij",
                cohortStatus = TrialStatus.OPEN,
                cohortSlotsNumberAvailable = 1
            )
        )
    }
}