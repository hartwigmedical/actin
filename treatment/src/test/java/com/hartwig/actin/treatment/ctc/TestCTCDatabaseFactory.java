package com.hartwig.actin.treatment.ctc;

import org.jetbrains.annotations.NotNull;

public final class TestCTCDatabaseFactory {

    @NotNull
    public static CTCDatabase createMinimalTestCTCDatabase() {
        return ImmutableCTCDatabase.builder().build();
    }
}
