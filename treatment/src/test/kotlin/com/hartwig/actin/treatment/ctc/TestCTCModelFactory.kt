package com.hartwig.actin.treatment.ctc

import com.hartwig.actin.treatment.ctc.config.TestCTCDatabaseFactory

object TestCTCModelFactory {
    fun createWithMinimalTestCTCDatabase(): CTCModel {
        return CTCModel(TestCTCDatabaseFactory.createMinimalTestCTCDatabase())
    }

    fun createWithProperTestCTCDatabase(): CTCModel {
        return CTCModel(TestCTCDatabaseFactory.createProperTestCTCDatabase())
    }
}