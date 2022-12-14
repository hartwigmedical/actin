package com.hartwig.actin.molecular.orange.datamodel.peach;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestPeachFactory {

    private TestPeachFactory() {
    }

    @NotNull
    public static ImmutablePeachEntry.Builder builder() {
        return ImmutablePeachEntry.builder().gene(Strings.EMPTY).haplotype(Strings.EMPTY).function(Strings.EMPTY);
    }
}
