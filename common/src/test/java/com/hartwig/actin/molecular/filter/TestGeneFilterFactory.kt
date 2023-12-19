package com.hartwig.actin.molecular.filter;

import com.google.common.collect.Sets;

import org.jetbrains.annotations.NotNull;

public final class TestGeneFilterFactory {

    private TestGeneFilterFactory() {
    }

    @NotNull
    public static GeneFilter createAlwaysValid() {
        return new AlwaysValidFilter();
    }

    @NotNull
    public static GeneFilter createNeverValid() {
        return new SpecificGenesFilter(Sets.newHashSet());
    }

    @NotNull
    public static GeneFilter createValidForGenes(@NotNull String... genes) {
        return new SpecificGenesFilter(Sets.newHashSet(genes));
    }
}
