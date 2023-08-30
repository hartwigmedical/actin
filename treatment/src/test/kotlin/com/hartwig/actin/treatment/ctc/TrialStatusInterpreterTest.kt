package com.hartwig.actin.treatment.ctc

import com.hartwig.actin.treatment.ctc.TrialStatusInterpreter.isOpen
import com.hartwig.actin.treatment.ctc.config.CTCDatabaseEntry
import com.hartwig.actin.treatment.ctc.config.TestCTCDatabaseEntryFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialStatusInterpreterTest {

    @Test
    fun shouldReturnNullForEmptyCTCDatabase() {
        assertThat(isOpen(listOf(), "trial 1")).isNull()
    }

    @Test
    fun shouldResolveToOpenForTrialsWithExclusivelyOpenEntries() {
        val openMETC1 = createEntry(STUDY_METC_1, "Open")
        val closedMETC2 = createEntry(STUDY_METC_2, "Gesloten")
        assertThat(isOpen(listOf(openMETC1, closedMETC2), CTCModel.constructTrialId(openMETC1))).isTrue
    }

    @Test
    fun shouldResolveToClosedForTrialsWithInconsistentEntries() {
        val openMETC1 = createEntry(STUDY_METC_1, "Open")
        val closedMETC1 = createEntry(STUDY_METC_1, "Gesloten")
        assertThat(isOpen(listOf(openMETC1, closedMETC1), CTCModel.constructTrialId(closedMETC1))).isFalse
    }

    @Test
    fun shouldResolveToClosedForTrialsWithClosedEntriesExclusively() {
        val closedMETC1 = createEntry(STUDY_METC_1, "Gesloten")
        val openMETC2 = createEntry(STUDY_METC_2, "Open")
        assertThat(isOpen(listOf(closedMETC1, openMETC2), CTCModel.constructTrialId(closedMETC1))).isFalse
    }

    companion object {
        private const val STUDY_METC_1 = "1"
        private const val STUDY_METC_2 = "2"

        private fun createEntry(studyMETC: String, studyStatus: String): CTCDatabaseEntry {
            return TestCTCDatabaseEntryFactory.MINIMAL.copy(studyMETC = studyMETC, studyStatus = studyStatus)
        }
    }
}