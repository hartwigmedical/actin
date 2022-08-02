package com.hartwig.actin.doid.config;

import org.jetbrains.annotations.NotNull;

public final class TestDoidManualConfigFactory {

    private TestDoidManualConfigFactory() {
    }

    @NotNull
    public static DoidManualConfig createMinimalTestDoidManualConfig() {
        return ImmutableDoidManualConfig.builder().build();
    }

    @NotNull
    public static DoidManualConfig createWithOneMainCancerDoid(@NotNull String mainCancerDoid) {
        return ImmutableDoidManualConfig.builder().addMainCancerDoids(mainCancerDoid).build();
    }

    @NotNull
    public static DoidManualConfig createWithOneAdenoSquamousMapping(@NotNull AdenoSquamousMapping mapping) {
        return ImmutableDoidManualConfig.builder().addAdenoSquamousMappings(mapping).build();
    }
}
