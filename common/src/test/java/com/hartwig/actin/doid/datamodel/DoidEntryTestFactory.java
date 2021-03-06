package com.hartwig.actin.doid.datamodel;

import org.jetbrains.annotations.NotNull;

public final class DoidEntryTestFactory {

    private DoidEntryTestFactory() {
    }

    @NotNull
    public static DoidEntry createMinimalTestDoidEntry() {
        return ImmutableDoidEntry.builder().id("TEST-DOID").metadata(ImmutableGraphMetadata.builder().build()).build();
    }
}
