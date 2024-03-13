package com.hartwig.actin.trial.status

import com.hartwig.actin.trial.status.config.TestCTCDatabaseFactory

object TestCTCModelFactory {

    fun createWithMinimalTestCTCDatabase(): TrialStatusConfigInterpreter {
        return TrialStatusConfigInterpreter(TestCTCDatabaseFactory.createMinimalTestCTCDatabase())
    }

    fun createWithProperTestCTCDatabase(): TrialStatusConfigInterpreter {
        return TrialStatusConfigInterpreter(TestCTCDatabaseFactory.createProperTestCTCDatabase())
    }
}