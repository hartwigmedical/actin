package com.hartwig.actin.molecular.orange.filter;

import com.google.common.collect.Sets;

import org.jetbrains.annotations.NotNull;

public final class TestGeneFilterFactory {

    private TestGeneFilterFactory() {
    }

    @NotNull
    public static GeneFilter createEmpty() {
        return new GeneFilter(Sets.newHashSet());
    }
}
