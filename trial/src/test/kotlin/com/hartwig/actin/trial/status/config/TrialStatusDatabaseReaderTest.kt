package com.hartwig.actin.trial.status.config

import com.hartwig.actin.testutil.ResourceLocator.resourceOnClasspath
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
        assertStudiesNotInTrialStatusDatabase(database.studiesNotInTrialStatusDatabase)
    }

    companion object {
        private val CTC_CONFIG_DIRECTORY = resourceOnClasspath("ctc_config")

        private fun assertEntries(entries: List<TrialStatusEntry>) {
            assertThat(entries).hasSize(2)
            val entry1 = findEntryByStudyId(entries, "METC 1")
            assertThat(entry1.metcStudyID).isEqualTo("METC 1")
            assertThat(entry1.studyStatus).isEqualTo(TrialStatus.OPEN)
            assertThat((entry1.cohortId as String).toLong()).isEqualTo(1)
            assertThat((entry1.cohortParentId as String).toLong()).isEqualTo(2)
            assertThat(entry1.cohortStatus).isEqualTo(TrialStatus.CLOSED)
            assertThat((entry1.cohortSlotsNumberAvailable as Int).toLong()).isEqualTo(5)
            assertThat(entry1.cohortSlotsDateUpdate).isEqualTo("23-04-04")

            val entry2 = findEntryByStudyId(entries, "METC 2")
            assertThat(entry2.metcStudyID).isEqualTo("METC 2")
            assertThat(entry2.studyStatus).isEqualTo(TrialStatus.CLOSED)
            assertThat(entry2.cohortId).isNull()
            assertThat(entry2.cohortParentId).isNull()
            assertThat(entry2.cohortStatus).isNull()
            assertThat(entry2.cohortSlotsNumberAvailable).isNull()
            assertThat(entry2.cohortSlotsDateUpdate).isNull()
        }

        private fun findEntryByStudyId(entries: List<TrialStatusEntry>, metcToFind: String): TrialStatusEntry {
            return entries.first { it.metcStudyID == metcToFind }
        }

        private fun assertStudyMETCsToIgnore(studyMETCsToIgnore: Set<String>) {
            assertThat(studyMETCsToIgnore).hasSize(2)
            assertThat(studyMETCsToIgnore.contains("METC 1")).isTrue
            assertThat(studyMETCsToIgnore.contains("METC 10")).isTrue
        }

        private fun assertUnmappedCohortIds(unmappedCohortIds: Set<String>) {
            assertThat(unmappedCohortIds).hasSize(1)
            assertThat(unmappedCohortIds.contains("1")).isTrue
        }

        private fun assertStudiesNotInTrialStatusDatabase(studiesNotInTrialStatusDatabase: Set<String>) {
            assertThat(studiesNotInTrialStatusDatabase).hasSize(1)
            assertThat(studiesNotInTrialStatusDatabase.contains("ACTN 2021")).isTrue
        }
    }
}