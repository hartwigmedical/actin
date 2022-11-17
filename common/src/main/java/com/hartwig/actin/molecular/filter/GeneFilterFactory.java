package com.hartwig.actin.molecular.filter;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.serve.KnownGene;

import org.jetbrains.annotations.NotNull;

public final class GeneFilterFactory {

    private GeneFilterFactory() {
    }

    @NotNull
    public static GeneFilter createAlwaysValid() {
        return new AlwaysValidFilter();
    }

    @NotNull
    public static GeneFilter createFromKnownGenes(@NotNull List<KnownGene> knownGenes) {
        Set<String> genes = Sets.newHashSet();
        for (KnownGene knownGene : knownGenes) {
            genes.add(knownGene.gene());
        }

        return new SpecificGenesFilter(genes);
    }
}
