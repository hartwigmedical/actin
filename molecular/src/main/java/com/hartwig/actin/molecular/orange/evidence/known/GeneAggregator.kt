package com.hartwig.actin.molecular.orange.evidence.known

import com.hartwig.serve.datamodel.common.GeneRole
import com.hartwig.serve.datamodel.gene.KnownGene
import java.util.function.Function
import java.util.stream.Collectors

internal object GeneAggregator {
    private val ROLE_PRECEDENCE = listOf(GeneRole.BOTH, GeneRole.ONCO, GeneRole.TSG, GeneRole.UNKNOWN)
    fun aggregate(rawGenes: Collection<KnownGene>): MutableSet<KnownGene> {
        return rawGenes.stream()
            .collect(Collectors.groupingBy(Function { obj: KnownGene -> obj.gene() }))
            .values
            .stream()
            .map { genes: MutableList<KnownGene> -> genes.stream().min(genePrecedenceOrder()).orElseThrow() }
            .collect(Collectors.toSet())
    }

    private fun genePrecedenceOrder(): Comparator<KnownGene> {
        return Comparator.comparingInt { o: KnownGene -> ROLE_PRECEDENCE.indexOf(o.geneRole()) }
    }
}
