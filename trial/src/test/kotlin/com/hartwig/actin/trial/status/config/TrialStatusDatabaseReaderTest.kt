package com.hartwig.actin.trial.status.config

import com.hartwig.actin.testutil.ResourceLocator
import com.hartwig.actin.trial.status.TrialStatus
import com.hartwig.actin.trial.status.TrialStatusDatabaseReader
import com.hartwig.actin.trial.status.TrialStatusEntry
import com.hartwig.actin.trial.status.ctc.CTCTrialStatusEntryReader
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialStatusDatabaseReaderTest {

    @Test
    fun shouldLoadExpectedDatabaseFromTestDirectory() {
        val database = TrialStatusDatabaseReader(CTCTrialStatusEntryReader()).read(CTC_CONFIG_DIRECTORY)

        assertEntries(database.entries)
        assertStudyMETCsToIgnore(database.studyMETCsToIgnore)
        assertUnmappedCohortIds(database.unmappedCohortIds)
        assertStudyNotInCTC(database.studiesNotInTrialStatusDatabase)
    }

    companion object {
        private val CTC_CONFIG_DIRECTORY = ResourceLocator().onClasspath("ctc_config")

        private fun assertEntries(entries: List<TrialStatusEntry>) {
            assertThat(entries).hasSize(2)
            val entry1 = findEntryByStudyId(entries, 1)
            assertThat(entry1.metcStudyID).isEqualTo("METC 1")
            assertThat(entry1.studyAcronym).isEqualTo("StudyWithCohort")
            assertThat(entry1.studyTitle).isEqualTo("This is a study with cohort")
            assertThat(entry1.studyStatus).isEqualTo(TrialStatus.OPEN)
            assertThat((entry1.cohortId as Int).toLong()).isEqualTo(1)
            assertThat((entry1.cohortParentId as Int).toLong()).isEqualTo(2)
            assertThat(entry1.cohortName).isEqualTo("Cohort A")
            assertThat(entry1.cohortStatus).isEqualTo(TrialStatus.CLOSED)
            assertThat((entry1.cohortSlotsNumberAvailable as Int).toLong()).isEqualTo(5)
            assertThat(entry1.cohortSlotsDateUpdate).isEqualTo("23-04-04")

            val entry2 = findEntryByStudyId(entries, 2)
            assertThat(entry2.metcStudyID).isEqualTo("METC 2")
            assertThat(entry2.studyAcronym).isEqualTo("StudyWithoutCohort")
            assertThat(entry2.studyTitle).isEqualTo("This is a study without cohort")
            assertThat(entry2.studyStatus).isEqualTo(TrialStatus.CLOSED)
            assertThat(entry2.cohortId).isNull()
            assertThat(entry2.cohortParentId).isNull()
            assertThat(entry2.cohortName).isNull()
            assertThat(entry2.cohortStatus).isNull()
            assertThat(entry2.cohortSlotsNumberAvailable).isNull()
            assertThat(entry2.cohortSlotsDateUpdate).isNull()
        }

        private fun findEntryByStudyId(entries: List<TrialStatusEntry>, studyIdToFind: Int): TrialStatusEntry {
            return entries.first { it.studyId == studyIdToFind }
        }

        private fun assertStudyMETCsToIgnore(studyMETCsToIgnore: Set<String>) {
            assertThat(studyMETCsToIgnore).hasSize(1)
            assertThat(studyMETCsToIgnore.contains("METC 1")).isTrue
        }

        private fun assertUnmappedCohortIds(unmappedCohortIds: Set<Int>) {
            assertThat(unmappedCohortIds).hasSize(1)
            assertThat(unmappedCohortIds.contains(1)).isTrue
        }

        private fun assertStudyNotInCTC(studyWithMECIdNotInCTC: Set<String>) {
            assertThat(studyWithMECIdNotInCTC).hasSize(1)
            assertThat(studyWithMECIdNotInCTC.contains("ACTN 2021")).isTrue
        }
    }
}