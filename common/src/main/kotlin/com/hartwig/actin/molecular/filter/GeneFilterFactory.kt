package com.hartwig.actin.molecular.filter

import com.hartwig.serve.datamodel.molecular.gene.KnownGene


object GeneFilterFactory {

    fun createAlwaysValid(): GeneFilter {
        return AlwaysValidFilter()
    }

    fun createFromKnownGenes(knownGenes: Collection<KnownGene>): GeneFilter {
        return SpecificGenesFilter(knownGenes.map(KnownGene::gene).toSet())
    }
}
