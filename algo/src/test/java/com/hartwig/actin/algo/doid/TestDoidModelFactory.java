package com.hartwig.actin.algo.doid;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import org.jetbrains.annotations.NotNull;

public final class TestDoidModelFactory {

    private TestDoidModelFactory() {
    }

    @NotNull
    public static DoidModel createMinimalTestDoidModel() {
        return new DoidModel(ArrayListMultimap.create());
    }

    @NotNull
    public static DoidModel createWithOneParentChild(@NotNull String parent, @NotNull String child) {
        Multimap<String, String> relationship = ArrayListMultimap.create();
        relationship.put(child, parent);
        return new DoidModel(relationship);
    }
}
