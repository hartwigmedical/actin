package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.config.CohortDefinitionConfig
import com.hartwig.actin.trial.config.TestCohortDefinitionConfigFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CohortStatusInterpreterTest {

    @Test
    fun `Should ignore cohorts that are configured as not available`() {
        val notAvailable = createWithExternalCohortIDs(CohortStatusInterpreter.NOT_AVAILABLE)
        assertThat(evaluate(notAvailable)).isNull()
    }

    @Test
    fun `Should ignore cohorts that are configured as not in trial status database unknown why`() {
        val notInTrialStatusDatabaseUnknownWhy =
            createWithExternalCohortIDs(CohortStatusInterpreter.NOT_IN_TRIAL_STATUS_DATABASE_OVERVIEW_UNKNOWN_WHY)
        assertThat(evaluate(notInTrialStatusDatabaseUnknownWhy)).isNull()
    }

    @Test
    fun `Should ignore cohorts that are configured as overruled because incorrect in trial status database`() {
        val overruledBecauseIncorrectInTrialStatusDatabase =
            createWithExternalCohortIDs(CohortStatusInterpreter.OVERRULED_BECAUSE_INCORRECT_IN_TRIAL_STATUS_DATABASE)
        assertThat(evaluate(overruledBecauseIncorrectInTrialStatusDatabase)).isNull()
    }

    @Test
    fun `Should assume unmapped closed cohorts are closed`() {
        val notMappedClosed = createWithExternalCohortIDs(CohortStatusInterpreter.WONT_BE_MAPPED_BECAUSE_CLOSED)
        val status = evaluate(notMappedClosed)
        assertThat(status!!.open).isFalse
        assertThat(status.slotsAvailable).isFalse
    }

    @Test
    fun `Should assume unmapped unavailable cohorts are closed`() {
        val notMappedNotAvailable = createWithExternalCohortIDs(CohortStatusInterpreter.WONT_BE_MAPPED_BECAUSE_NOT_AVAILABLE)
        val status = evaluate(notMappedNotAvailable)
        assertThat(status!!.open).isFalse
        assertThat(status.slotsAvailable).isFalse
    }

    @Test(expected = NumberFormatException::class)
    fun `Should throw exception on unexpected non integer cohort id`() {
        val unexpected = createWithExternalCohortIDs("this is unexpected")
        evaluate(unexpected)
    }

    companion object {
        private fun evaluate(cohortConfig: CohortDefinitionConfig): InterpretedCohortStatus? {
            return CohortStatusInterpreter.interpret(listOf(), cohortConfig).status
        }

        private fun createWithExternalCohortIDs(vararg externalCohortIDs: String): CohortDefinitionConfig {
            return TestCohortDefinitionConfigFactory.MINIMAL.copy(externalCohortIds = setOf(*externalCohortIDs))
        }
    }
}