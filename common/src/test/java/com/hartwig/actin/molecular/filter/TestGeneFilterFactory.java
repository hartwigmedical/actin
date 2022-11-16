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
    public static GeneFilter createValidForGene(@NotNull String gene) {
        return new SpecificGenesFilter(Sets.newHashSet(gene));
    }

    @NotNull
    public static GeneFilter createNeverValid() {
        return new SpecificGenesFilter(Sets.newHashSet());
    }
}
