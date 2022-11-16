package com.hartwig.actin.molecular.filter;

import java.util.Set;

import com.google.common.annotations.VisibleForTesting;

import org.jetbrains.annotations.NotNull;

class SpecificGenesFilter implements GeneFilter {

    @NotNull
    private final Set<String> allowedGenes;

    SpecificGenesFilter(@NotNull final Set<String> allowedGenes) {
        this.allowedGenes = allowedGenes;
    }

    @NotNull
    @VisibleForTesting
    Set<String> allowedGenes() {
        return allowedGenes;
    }

    @Override
    public boolean include(@NotNull final String gene) {
        return allowedGenes.contains(gene);
    }

    @Override
    public int size() {
        return allowedGenes.size();
    }
}
