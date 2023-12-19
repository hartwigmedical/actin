package com.hartwig.actin.molecular.filter

import com.hartwig.serve.datamodel.gene.KnownGene
import java.util.stream.Collectors

object GeneFilterFactory {
    @JvmStatic
    fun createAlwaysValid(): GeneFilter {
        return AlwaysValidFilter()
    }

    fun createFromKnownGenes(knownGenes: Collection<KnownGene>): GeneFilter {
        return SpecificGenesFilter(knownGenes.stream().map { obj: KnownGene -> obj.gene() }.collect(Collectors.toSet()))
    }
}
