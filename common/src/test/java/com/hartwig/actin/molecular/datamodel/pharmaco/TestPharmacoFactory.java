package com.hartwig.actin.molecular.datamodel.pharmaco;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestPharmacoFactory {

    private TestPharmacoFactory() {
    }

    @NotNull
    public static ImmutableHaplotype.Builder builder() {
        return ImmutableHaplotype.builder().name(Strings.EMPTY).function(Strings.EMPTY);
    }
}
