package com.hartwig.actin.molecular.orange.filter;

import java.util.Set;

import com.google.common.annotations.VisibleForTesting;

import org.jetbrains.annotations.NotNull;

public class GeneFilter {

    @NotNull
    private final Set<String> allowedGenes;

    GeneFilter(@NotNull final Set<String> allowedGenes) {
        this.allowedGenes = allowedGenes;
    }

    @NotNull
    @VisibleForTesting
    Set<String> allowedGenes() {
        return allowedGenes;
    }

    public boolean include(@NotNull String gene) {
        return allowedGenes.contains(gene);
    }

    public int size() {
        return allowedGenes.size();
    }
}
