package com.hartwig.actin.treatment.ctc.config

import com.google.common.io.Resources
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CTCDatabaseReaderTest {

    @Test
    fun shouldLoadExpectedDatabaseFromTestDirectory() {
        val database = CTCDatabaseReader.read(CTC_CONFIG_DIRECTORY)

        assertEntries(database.entries)
        assertStudyMETCsToIgnore(database.studyMETCsToIgnore)
        assertUnmappedCohortIds(database.unmappedCohortIds)
    }

    companion object {
        private val CTC_CONFIG_DIRECTORY = Resources.getResource("ctc_config").path

        private fun assertEntries(entries: List<CTCDatabaseEntry>) {
            assertThat(entries).hasSize(2)
            val entry1 = findEntryByStudyId(entries, 1)
            assertThat(entry1.studyMETC).isEqualTo("METC 1")
            assertThat(entry1.studyAcronym).isEqualTo("StudyWithCohort")
            assertThat(entry1.studyTitle).isEqualTo("This is a study with cohort")
            assertThat(entry1.studyStatus).isEqualTo("Open")
            assertThat((entry1.cohortId as Int).toLong()).isEqualTo(1)
            assertThat((entry1.cohortParentId as Int).toLong()).isEqualTo(2)
            assertThat(entry1.cohortName).isEqualTo("Cohort A")
            assertThat(entry1.cohortStatus).isEqualTo("Closed")
            assertThat((entry1.cohortSlotsNumberAvailable as Int).toLong()).isEqualTo(5)
            assertThat(entry1.cohortSlotsDateAvailable).isEqualTo("23-04-04")

            val entry2 = findEntryByStudyId(entries, 2)
            assertThat(entry2.studyMETC).isEqualTo("METC 2")
            assertThat(entry2.studyAcronym).isEqualTo("StudyWithoutCohort")
            assertThat(entry2.studyTitle).isEqualTo("This is a study without cohort")
            assertThat(entry2.studyStatus).isEqualTo("Closed")
            assertThat(entry2.cohortId).isNull()
            assertThat(entry2.cohortParentId).isNull()
            assertThat(entry2.cohortName).isNull()
            assertThat(entry2.cohortStatus).isNull()
            assertThat(entry2.cohortSlotsNumberAvailable).isNull()
            assertThat(entry2.cohortSlotsDateAvailable).isNull()
        }

        private fun findEntryByStudyId(entries: List<CTCDatabaseEntry>, studyIdToFind: Int): CTCDatabaseEntry {
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
    }
}