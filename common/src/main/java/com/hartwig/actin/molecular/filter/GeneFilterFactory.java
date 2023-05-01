package com.hartwig.actin.molecular.filter;

import java.util.Collection;
import java.util.stream.Collectors;

import com.hartwig.serve.datamodel.gene.KnownGene;

import org.jetbrains.annotations.NotNull;

public final class GeneFilterFactory {

    private GeneFilterFactory() {
    }

    @NotNull
    public static GeneFilter createAlwaysValid() {
        return new AlwaysValidFilter();
    }

    @NotNull
    public static GeneFilter createFromKnownGenes(@NotNull Collection<KnownGene> knownGenes) {
        return new SpecificGenesFilter(GeneAggregator.aggregate(knownGenes).stream().map(KnownGene::gene).collect(Collectors.toSet()));
    }
}
