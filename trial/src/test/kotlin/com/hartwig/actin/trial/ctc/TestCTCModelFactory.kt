package com.hartwig.actin.trial.ctc

import com.hartwig.actin.trial.ctc.config.TestCTCDatabaseFactory

object TestCTCModelFactory {

    fun createWithMinimalTestCTCDatabase(): EmcCtcModel {
        return EmcCtcModel(TestCTCDatabaseFactory.createMinimalTestCTCDatabase())
    }

    fun createWithProperTestCTCDatabase(): EmcCtcModel {
        return EmcCtcModel(TestCTCDatabaseFactory.createProperTestCTCDatabase())
    }
}