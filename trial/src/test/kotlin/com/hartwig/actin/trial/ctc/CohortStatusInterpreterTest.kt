package com.hartwig.actin.trial.ctc

import com.hartwig.actin.trial.trial.config.CohortDefinitionConfig
import com.hartwig.actin.trial.trial.config.TestCohortDefinitionConfigFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CohortStatusInterpreterTest {

    @Test
    fun shouldIgnoreCohortsThatAreConfiguredAsNotAvailable() {
        val notAvailable = createWithCTCCohortIDs(CohortStatusInterpreter.NOT_AVAILABLE)
        assertThat(evaluate(notAvailable)).isNull()
    }

    @Test
    fun shouldIgnoreCohortsThatAreConfiguredAsNotInCTCOverviewUnknownWhy() {
        val notInCTCOverviewUnknownWhy = createWithCTCCohortIDs(CohortStatusInterpreter.NOT_IN_CTC_OVERVIEW_UNKNOWN_WHY)
        assertThat(evaluate(notInCTCOverviewUnknownWhy)).isNull()
    }

    @Test
    fun shouldIgnoreCohortsThatAreConfiguredAsOverruledBecauseIncorrectInCTC() {
        val overruledBecauseIncorrectInCTC =
            createWithCTCCohortIDs(CohortStatusInterpreter.OVERRULED_BECAUSE_INCORRECT_IN_CTC)
        assertThat(evaluate(overruledBecauseIncorrectInCTC)).isNull()
    }

    @Test
    fun shouldAssumeUnmappedClosedCohortsAreClosed() {
        val notMappedClosed = createWithCTCCohortIDs(CohortStatusInterpreter.WONT_BE_MAPPED_BECAUSE_CLOSED)
        val status = evaluate(notMappedClosed)
        assertThat(status!!.open).isFalse
        assertThat(status.slotsAvailable).isFalse
    }

    @Test
    fun shouldAssumeUnmappedUnavailableCohortsAreClosed() {
        val notMappedNotAvailable = createWithCTCCohortIDs(CohortStatusInterpreter.WONT_BE_MAPPED_BECAUSE_NOT_AVAILABLE)
        val status = evaluate(notMappedNotAvailable)
        assertThat(status!!.open).isFalse
        assertThat(status.slotsAvailable).isFalse
    }

    @Test(expected = NumberFormatException::class)
    fun shouldThrowExceptionOnUnexpectedNonIntegerCohortId() {
        val unexpected = createWithCTCCohortIDs("this is unexpected")
        evaluate(unexpected)
    }

    companion object {
        private fun evaluate(cohortConfig: CohortDefinitionConfig): InterpretedCohortStatus? {
            return CohortStatusInterpreter.interpret(listOf(), cohortConfig)
        }

        private fun createWithCTCCohortIDs(vararg ctcCohortIDs: String): CohortDefinitionConfig {
            return TestCohortDefinitionConfigFactory.MINIMAL.copy(ctcCohortIds = setOf(*ctcCohortIDs))
        }
    }
}