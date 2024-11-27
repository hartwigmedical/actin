package com.hartwig.actin.trial.status.config

import com.hartwig.actin.testutil.ResourceLocator.resourceOnClasspath
import com.hartwig.actin.trial.status.CohortStatusEntry
import com.hartwig.actin.trial.status.TrialStatus
import com.hartwig.actin.trial.status.TrialStatusDatabaseReader
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialStatusDatabaseReaderTest {

    @Test
    fun shouldLoadExpectedDatabaseFromTestDirectory() {
        val database = TrialStatusDatabaseReader().read(CONFIG_DIRECTORY)

        assertEntries(database.entries)
    }

    companion object {
        private val CONFIG_DIRECTORY = resourceOnClasspath("status_config")

        private fun assertEntries(entries: List<CohortStatusEntry>) {
            assertThat(entries).hasSize(2)
            val entry1 = findEntryByNctId(entries, "NCT1")
            assertThat(entry1.nctId).isEqualTo("NCT1")
            assertThat(entry1.trialStatus).isEqualTo(TrialStatus.OPEN)
            assertThat(entry1.cohortId.toLong()).isEqualTo(1)
            assertThat((entry1.cohortParentId as String).toLong()).isEqualTo(2)
            assertThat(entry1.cohortStatus).isEqualTo(TrialStatus.CLOSED)
            assertThat((entry1.cohortSlotsNumberAvailable as Int).toLong()).isEqualTo(5)
            assertThat(entry1.cohortSlotsDateUpdate).isEqualTo("23-04-04")

            val entry2 = findEntryByNctId(entries, "NCT2")
            assertThat(entry2.nctId).isEqualTo("NCT2")
            assertThat(entry2.trialStatus).isEqualTo(TrialStatus.CLOSED)
            assertThat(entry2.cohortId).isEqualTo("2")
            assertThat(entry2.cohortParentId).isNull()
            assertThat(entry2.cohortStatus).isEqualTo(TrialStatus.CLOSED)
            assertThat(entry2.cohortSlotsNumberAvailable).isNull()
            assertThat(entry2.cohortSlotsDateUpdate).isNull()
        }

        private fun findEntryByNctId(entries: List<CohortStatusEntry>, nctId: String): CohortStatusEntry {
            return entries.first { it.nctId == nctId }
        }
    }
}