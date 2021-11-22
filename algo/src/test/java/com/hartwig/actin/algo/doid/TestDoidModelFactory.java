package com.hartwig.actin.algo.doid;

import com.google.common.collect.ArrayListMultimap;

import org.jetbrains.annotations.NotNull;

public final class TestDoidModelFactory {

    private TestDoidModelFactory() {
    }

    @NotNull
    public static DoidModel createMinimalTestDoidModel() {
        return new DoidModel(ArrayListMultimap.create());
    }
}
