package com.hartwig.actin.molecular.filter;

import java.util.Collection;
import java.util.stream.Collectors;

import com.hartwig.serve.datamodel.common.GeneRole;
import com.hartwig.serve.datamodel.gene.KnownGene;

import org.jetbrains.annotations.NotNull;

public final class GeneAggregator {

    public static Collection<KnownGene> aggregate(@NotNull Collection<KnownGene> rawGenes) {
        return rawGenes.stream()
                .collect(Collectors.groupingBy(KnownGene::gene))
                .values()
                .stream()
                .map(genes -> genes.stream()
                        .filter(gene -> !gene.geneRole().equals(GeneRole.UNKNOWN))
                        .findFirst()
                        .orElse(genes.iterator().next()))
                .collect(Collectors.toSet());
    }
}
