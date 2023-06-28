package com.hartwig.actin.treatment.ctc;

import com.hartwig.actin.treatment.ctc.config.TestCTCDatabaseFactory;

import org.jetbrains.annotations.NotNull;

public final class TestCTCModelFactory {

    @NotNull
    public static CTCModel createWithMinimalTestCTCDatabase() {
        return new CTCModel(TestCTCDatabaseFactory.createMinimalTestCTCDatabase());
    }

    @NotNull
    public static CTCModel createWithProperTestCTCDatabase() {
        return new CTCModel(TestCTCDatabaseFactory.createProperTestCTCDatabase());
    }
}
