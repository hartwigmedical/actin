package com.hartwig.actin.algo.doid.datamodel;

import org.jetbrains.annotations.NotNull;

public final class TestDoidFactory {

    private TestDoidFactory() {
    }

    @NotNull
    public static DoidEntry createMinimalTestDoidEntry() {
        return ImmutableDoidEntry.builder().id("TEST-DOID").metadata(ImmutableGraphMetadata.builder().build()).build();
    }
}
