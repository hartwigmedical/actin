package com.hartwig.actin.molecular.orange.evidence.known;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.hartwig.serve.datamodel.common.GeneRole;
import com.hartwig.serve.datamodel.gene.KnownGene;

import org.jetbrains.annotations.NotNull;

final class GeneAggregator {

    private static final List<GeneRole> ROLE_PRECEDENCE = List.of(GeneRole.BOTH, GeneRole.ONCO, GeneRole.TSG, GeneRole.UNKNOWN);

    static Set<KnownGene> aggregate(@NotNull Collection<KnownGene> rawGenes) {
        return rawGenes.stream()
                .collect(Collectors.groupingBy(KnownGene::gene))
                .values()
                .stream()
                .map(genes -> genes.stream().min(genePrecedenceOrder()).orElseThrow())
                .collect(Collectors.toSet());
    }

    @NotNull
    private static Comparator<KnownGene> genePrecedenceOrder() {
        return Comparator.comparingInt(o -> ROLE_PRECEDENCE.indexOf(o.geneRole()));
    }
}
