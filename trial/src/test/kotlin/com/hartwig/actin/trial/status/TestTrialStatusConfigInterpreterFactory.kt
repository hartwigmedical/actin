package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.CTC_TRIAL_PREFIX
import com.hartwig.actin.trial.status.config.TestTrialStatusDatabaseFactory

object TestTrialStatusConfigInterpreterFactory {

    fun createWithMinimalTestTrialStatusDatabase(): TrialStatusConfigInterpreter {
        return TrialStatusConfigInterpreter(TestTrialStatusDatabaseFactory.createMinimalTestTrialStatusDatabase(), CTC_TRIAL_PREFIX)
    }

    fun createWithProperTestTrialStatusDatabase(): TrialStatusConfigInterpreter {
        return TrialStatusConfigInterpreter(TestTrialStatusDatabaseFactory.createProperTestTrialStatusDatabase(), CTC_TRIAL_PREFIX)
    }
}