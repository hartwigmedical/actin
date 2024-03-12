package com.hartwig.actin.trial.ctc

import com.hartwig.actin.trial.config.CohortDefinitionConfig
import com.hartwig.actin.trial.config.TestCohortDefinitionConfigFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CohortStatusInterpreterTest {

    @Test
    fun `Should ignore cohorts that are configured as not available`() {
        val notAvailable = createWithCTCCohortIDs(CohortStatusInterpreter.NOT_AVAILABLE)
        assertThat(evaluate(notAvailable)).isNull()
    }

    @Test
    fun `Should ignore cohorts that are configured as not in CTC overview unknown why`() {
        val notInCTCOverviewUnknownWhy = createWithCTCCohortIDs(CohortStatusInterpreter.NOT_IN_CTC_OVERVIEW_UNKNOWN_WHY)
        assertThat(evaluate(notInCTCOverviewUnknownWhy)).isNull()
    }

    @Test
    fun `Should ignore cohorts that are configured as overruled because incorrect in CTC`() {
        val overruledBecauseIncorrectInCTC =
            createWithCTCCohortIDs(CohortStatusInterpreter.OVERRULED_BECAUSE_INCORRECT_IN_CTC)
        assertThat(evaluate(overruledBecauseIncorrectInCTC)).isNull()
    }

    @Test
    fun `Should assume unmapped closed cohorts are closed`() {
        val notMappedClosed = createWithCTCCohortIDs(CohortStatusInterpreter.WONT_BE_MAPPED_BECAUSE_CLOSED)
        val status = evaluate(notMappedClosed)
        assertThat(status!!.open).isFalse
        assertThat(status.slotsAvailable).isFalse
    }

    @Test
    fun `Should assume unmapped unavailable cohorts are closed`() {
        val notMappedNotAvailable = createWithCTCCohortIDs(CohortStatusInterpreter.WONT_BE_MAPPED_BECAUSE_NOT_AVAILABLE)
        val status = evaluate(notMappedNotAvailable)
        assertThat(status!!.open).isFalse
        assertThat(status.slotsAvailable).isFalse
    }

    @Test(expected = NumberFormatException::class)
    fun `Should throw exception on unexpected non integer cohort id`() {
        val unexpected = createWithCTCCohortIDs("this is unexpected")
        evaluate(unexpected)
    }

    companion object {
        private fun evaluate(cohortConfig: CohortDefinitionConfig): InterpretedCohortStatus? {
            return CohortStatusInterpreter.interpret(listOf(), cohortConfig).status
        }

        private fun createWithCTCCohortIDs(vararg ctcCohortIDs: String): CohortDefinitionConfig {
            return TestCohortDefinitionConfigFactory.MINIMAL.copy(externalCohortIds = setOf(*ctcCohortIDs))
        }
    }
}