package com.hartwig.actin.trial.ctc

import com.hartwig.actin.trial.ctc.config.TestCTCDatabaseFactory

object TestCTCModelFactory {

    fun createWithMinimalTestCTCDatabase(): CTCModel {
        return CTCModel(TestCTCDatabaseFactory.createMinimalTestCTCDatabase())
    }

    fun createWithProperTestCTCDatabase(): CTCModel {
        return CTCModel(TestCTCDatabaseFactory.createProperTestCTCDatabase())
    }
}