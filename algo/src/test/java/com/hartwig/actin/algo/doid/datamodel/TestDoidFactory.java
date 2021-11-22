package com.hartwig.actin.algo.doid.datamodel;

import com.google.common.collect.ListMultimap;
import com.hartwig.actin.algo.doid.DoidParents;

import org.jetbrains.annotations.NotNull;

public final class TestDoidFactory {

    private TestDoidFactory() {
    }

    @NotNull
    public static DoidParents createDoidParents(@NotNull ListMultimap<String, String> relationship) {
        return new DoidParents(relationship);
    }
}
