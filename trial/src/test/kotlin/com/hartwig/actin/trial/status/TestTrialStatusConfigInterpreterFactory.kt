package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.status.config.TestTrialStatusDatabaseFactory

object TestTrialStatusConfigInterpreterFactory {

    fun createWithMinimalTestTrialStatusDatabase(): TrialStatusConfigInterpreter {
        return TrialStatusConfigInterpreter(TestTrialStatusDatabaseFactory.createMinimalTestTrialStatusDatabase())
    }

    fun createWithProperTestTrialStatusDatabase(): TrialStatusConfigInterpreter {
        return TrialStatusConfigInterpreter(TestTrialStatusDatabaseFactory.createProperTestTrialStatusDatabase())
    }
}