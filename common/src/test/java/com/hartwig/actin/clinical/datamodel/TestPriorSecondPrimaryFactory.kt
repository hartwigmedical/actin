package com.hartwig.actin.clinical.datamodel;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestPriorSecondPrimaryFactory {

    private TestPriorSecondPrimaryFactory() {
    }

    @NotNull
    public static ImmutablePriorSecondPrimary.Builder builder() {
        return ImmutablePriorSecondPrimary.builder()
                .tumorLocation(Strings.EMPTY)
                .tumorSubLocation(Strings.EMPTY)
                .tumorType(Strings.EMPTY)
                .tumorSubType(Strings.EMPTY)
                .treatmentHistory(Strings.EMPTY)
                .status(TumorStatus.INACTIVE);
    }
}
