package com.hartwig.actin.trial.status.nki

import com.hartwig.actin.testutil.ResourceLocator.resourceOnClasspath
import com.hartwig.actin.trial.status.TrialStatus
import com.hartwig.actin.trial.status.TrialStatusEntry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NKITrialStatusEntryReaderTest {

    @Test
    fun `Should read all trial status from JSON and only include open, closed and suspended trials`() {
        val status = NKITrialStatusEntryReader().read(resourceOnClasspath("nki_config"))
        assertThat(status).containsExactly(
            TrialStatusEntry(
                studyId = 1,
                metcStudyID = "MEC-001",
                studyAcronym = "OPN-001",
                studyTitle = "Open trial",
                studyStatus = TrialStatus.OPEN,
                cohortId = "abcd",
                cohortStatus = TrialStatus.OPEN,
                cohortSlotsNumberAvailable = 1
            ),
            TrialStatusEntry(
                studyId = 1,
                metcStudyID = "MEC-001",
                studyAcronym = "OPN-001",
                studyTitle = "Open trial",
                studyStatus = TrialStatus.OPEN,
                cohortId = "bcde",
                cohortStatus = TrialStatus.OPEN,
                cohortSlotsNumberAvailable = 2
            ),
            TrialStatusEntry(
                studyId = 1,
                metcStudyID = "MEC-001",
                studyAcronym = "OPN-001",
                studyTitle = "Open trial",
                studyStatus = TrialStatus.OPEN,
                cohortId = "bcdef",
                cohortStatus = TrialStatus.CLOSED,
                cohortSlotsNumberAvailable = 3
            ), TrialStatusEntry(
                studyId = 1,
                metcStudyID = "MEC-001",
                studyAcronym = "OPN-001",
                studyTitle = "Open trial",
                studyStatus = TrialStatus.OPEN,
                cohortId = "bcdeg",
                cohortStatus = TrialStatus.CLOSED,
                cohortSlotsNumberAvailable = 4
            ), TrialStatusEntry(
                studyId = 2,
                metcStudyID = "MEC-002",
                studyAcronym = "CLS-001",
                studyTitle = "Closed trial",
                studyStatus = TrialStatus.CLOSED,
                cohortId = "cdef",
                cohortStatus = TrialStatus.OPEN,
                cohortSlotsNumberAvailable = 5
            ),
            TrialStatusEntry(
                studyId = 5,
                metcStudyID = "MEC-005",
                studyAcronym = "CLS-001",
                studyTitle = "Suspended trial",
                studyStatus = TrialStatus.CLOSED,
                cohortId = "ghij",
                cohortStatus = TrialStatus.OPEN,
                cohortSlotsNumberAvailable = 7
            )
        )
    }
}