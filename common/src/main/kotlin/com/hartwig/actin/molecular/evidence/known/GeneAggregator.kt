package com.hartwig.actin.molecular.evidence.known

import com.hartwig.serve.datamodel.common.GeneRole
import com.hartwig.serve.datamodel.gene.KnownGene

internal object GeneAggregator {

    private val ROLE_PRECEDENCE = listOf(GeneRole.BOTH, GeneRole.ONCO, GeneRole.TSG, GeneRole.UNKNOWN)

    fun aggregate(rawGenes: Collection<KnownGene>): Set<KnownGene> {
        return rawGenes.groupBy(KnownGene::gene).values
            .map { genes -> genes.minWith(genePrecedenceOrder()) }
            .toSet()
    }

    private fun genePrecedenceOrder(): Comparator<KnownGene> {
        return Comparator.comparingInt { ROLE_PRECEDENCE.indexOf(it.geneRole()) }
    }
}
