package com.hartwig.actin.trial.ctc

import com.hartwig.actin.trial.ctc.config.TestCTCDatabaseFactory

object TestCTCModelFactory {

    fun createWithMinimalTestCTCDatabase(): CTCConfigInterpreter {
        return CTCConfigInterpreter(TestCTCDatabaseFactory.createMinimalTestCTCDatabase())
    }

    fun createWithProperTestCTCDatabase(): CTCConfigInterpreter {
        return CTCConfigInterpreter(TestCTCDatabaseFactory.createProperTestCTCDatabase())
    }
}