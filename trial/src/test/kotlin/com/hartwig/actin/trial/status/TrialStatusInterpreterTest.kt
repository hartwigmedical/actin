package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.config.TrialDefinitionConfig
import com.hartwig.actin.trial.config.TrialDefinitionValidationError
import com.hartwig.actin.trial.status.config.TestTrialStatusDatabaseEntryFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TrialStatusInterpreterTest {

    @Test
    fun `Should return null for empty CTC Database`() {
        assertThat(
            TrialStatusInterpreter.isOpen(
                listOf(),
                TrialDefinitionConfig("trial-1", true, "", "", "")
            ) { it.metcStudyID }
        ).isEqualTo(null to emptyList<TrialDefinitionValidationError>())
    }

    @Test
    fun `Should resolve to open for trials with exclusively open entries`() {
        val openMETC1 = createEntry(STUDY_METC_1, TrialStatus.OPEN)
        val closedMETC2 = createEntry(STUDY_METC_2, TrialStatus.CLOSED)
        assertThat(
            TrialStatusInterpreter.isOpen(
                listOf(openMETC1, closedMETC2),
                TrialDefinitionConfig(openMETC1.metcStudyID, true, "", "", "")
            ) { it.metcStudyID }.first
        ).isTrue
    }

    @Test
    fun `Should resolve to closed for trials with inconsistent entries and return validation error`() {
        val openMETC1 = createEntry(STUDY_METC_1, TrialStatus.OPEN)
        val closedMETC1 = createEntry(STUDY_METC_1, TrialStatus.CLOSED)
        val config = TrialDefinitionConfig(closedMETC1.metcStudyID, false, ",", "", "")
        val (isOpen, validation) = TrialStatusInterpreter.isOpen(
            listOf(openMETC1, closedMETC1),
            config
        ) { it.metcStudyID }
        assertThat(isOpen).isFalse
        assertThat(validation).containsExactly(
            TrialDefinitionValidationError(
                config,
                "Inconsistent study status found in trial status database"
            )
        )
    }

    @Test
    fun `Should resolve to closed for trials with closed entries exclusively`() {
        val closedMETC1 = createEntry(STUDY_METC_1, TrialStatus.CLOSED)
        val openMETC2 = createEntry(STUDY_METC_2, TrialStatus.OPEN)
        assertThat(
            TrialStatusInterpreter.isOpen(
                listOf(closedMETC1, openMETC2),
                TrialDefinitionConfig(closedMETC1.metcStudyID, false, "", "", "")
            ) { it.metcStudyID }.first
        ).isFalse
    }

    companion object {
        private const val STUDY_METC_1 = "MEC 1"
        private const val STUDY_METC_2 = "MEC 2"

        private fun createEntry(studyMETC: String, studyStatus: TrialStatus): TrialStatusEntry {
            return TestTrialStatusDatabaseEntryFactory.MINIMAL.copy(metcStudyID = studyMETC, studyStatus = studyStatus)
        }
    }
}