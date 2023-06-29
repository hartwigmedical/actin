package com.hartwig.actin.treatment.ctc.config;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestCTCDatabaseEntryFactory {

    @NotNull
    public static ImmutableCTCDatabaseEntry.Builder builder() {
        return ImmutableCTCDatabaseEntry.builder()
                .studyId(0)
                .studyMETC(Strings.EMPTY)
                .studyAcronym(Strings.EMPTY)
                .studyTitle(Strings.EMPTY)
                .studyStatus(Strings.EMPTY);
    }
}
