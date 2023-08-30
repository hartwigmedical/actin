package com.hartwig.actin.treatment.ctc

import com.hartwig.actin.treatment.ctc.CohortStatusInterpreter.interpret
import com.hartwig.actin.treatment.trial.config.CohortDefinitionConfig
import com.hartwig.actin.treatment.trial.config.TestCohortDefinitionConfigFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CohortStatusInterpreterTest {

    @Test
    fun shouldIgnoreCohortsThatAreConfiguredAsNotAvailable() {
        val notAvailable = createWithCTCCohortIDs(CohortStatusInterpreter.NOT_AVAILABLE)
        assertThat(interpret(listOf(), notAvailable)).isNull()
    }

    @Test
    fun shouldIgnoreCohortsThatAreConfiguredAsNotInCTCOverviewUnknownWhy() {
        val notInCTCOverviewUnknownWhy = createWithCTCCohortIDs(CohortStatusInterpreter.NOT_IN_CTC_OVERVIEW_UNKNOWN_WHY)
        assertThat(interpret(listOf(), notInCTCOverviewUnknownWhy)).isNull()
    }

    @Test
    fun shouldIgnoreCohortsThatAreConfiguredAsOverruledBecauseIncorrectInCTC() {
        val overruledBecauseIncorrectInCTC =
            createWithCTCCohortIDs(CohortStatusInterpreter.OVERRULED_BECAUSE_INCORRECT_IN_CTC)
        assertThat(interpret(listOf(), overruledBecauseIncorrectInCTC)).isNull()
    }

    @Test
    fun shouldAssumeUnmappedClosedCohortsAreClosed() {
        val notMappedClosed = createWithCTCCohortIDs(CohortStatusInterpreter.WONT_BE_MAPPED_BECAUSE_CLOSED)
        val status = interpret(listOf(), notMappedClosed)
        assertThat(status!!.open).isFalse
        assertThat(status.slotsAvailable).isFalse
    }

    @Test
    fun shouldAssumeUnmappedUnavailableCohortsAreClosed() {
        val notMappedNotAvailable = createWithCTCCohortIDs(CohortStatusInterpreter.WONT_BE_MAPPED_BECAUSE_NOT_AVAILABLE)
        val status = interpret(listOf(), notMappedNotAvailable)
        assertThat(status!!.open).isFalse
        assertThat(status.slotsAvailable).isFalse
    }

    companion object {
        private fun createWithCTCCohortIDs(vararg ctcCohortIDs: String): CohortDefinitionConfig {
            return TestCohortDefinitionConfigFactory.MINIMAL.copy(ctcCohortIds = setOf(*ctcCohortIDs))
        }
    }
}