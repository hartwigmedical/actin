package com.hartwig.actin.trial.ctc

import com.hartwig.actin.trial.TrialDefinitionValidationError
import com.hartwig.actin.trial.config.TrialDefinitionConfig
import com.hartwig.actin.trial.ctc.config.CTCDatabaseEntry
import com.hartwig.actin.trial.ctc.config.TestCTCDatabaseEntryFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialStatusInterpreterTest {

    @Test
    fun shouldReturnNullForEmptyCTCDatabase() {
        assertThat(TrialStatusInterpreter.isOpen(listOf(), TrialDefinitionConfig("trial-1", true, "", ""))).isEqualTo(null to null)
    }

    @Test
    fun shouldResolveToOpenForTrialsWithExclusivelyOpenEntries() {
        val openMETC1 = createEntry(STUDY_METC_1, "Open")
        val closedMETC2 = createEntry(STUDY_METC_2, "Gesloten")
        assertThat(
            TrialStatusInterpreter.isOpen(
                listOf(openMETC1, closedMETC2),
                TrialDefinitionConfig(CTCModel.constructTrialId(openMETC1), true, "", "")
            ).first
        ).isTrue
    }

    @Test
    fun shouldResolveToClosedForTrialsWithInconsistentEntriesAndReturnValidationError() {
        val openMETC1 = createEntry(STUDY_METC_1, "Open")
        val closedMETC1 = createEntry(STUDY_METC_1, "Gesloten")
        val config = TrialDefinitionConfig(CTCModel.constructTrialId(closedMETC1), false, ",", "")
        val (isOpen, validation) = TrialStatusInterpreter.isOpen(
            listOf(openMETC1, closedMETC1),
            config
        )
        assertThat(isOpen).isFalse
        assertThat(validation).isEqualTo(TrialDefinitionValidationError(config, "Inconsistent study status found in CTC database"))
    }

    @Test
    fun shouldResolveToClosedForTrialsWithClosedEntriesExclusively() {
        val closedMETC1 = createEntry(STUDY_METC_1, "Gesloten")
        val openMETC2 = createEntry(STUDY_METC_2, "Open")
        assertThat(
            TrialStatusInterpreter.isOpen(
                listOf(closedMETC1, openMETC2),
                TrialDefinitionConfig(CTCModel.constructTrialId(closedMETC1), false, "", "")
            ).first
        ).isFalse
    }

    companion object {
        private const val STUDY_METC_1 = "1"
        private const val STUDY_METC_2 = "2"

        private fun createEntry(studyMETC: String, studyStatus: String): CTCDatabaseEntry {
            return TestCTCDatabaseEntryFactory.MINIMAL.copy(studyMETC = studyMETC, studyStatus = studyStatus)
        }
    }
}