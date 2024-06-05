package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.datamodel.orange.pharmaco.Haplotype
import com.hartwig.actin.molecular.datamodel.orange.pharmaco.PharmacoEntry
import com.hartwig.hmftools.datamodel.orange.OrangeRecord
import com.hartwig.hmftools.datamodel.peach.PeachGenotype

internal object PharmacoExtraction {

    fun extract(record: OrangeRecord): Set<PharmacoEntry> {
        val peach = record.peach() ?: return emptySet()
        return peach.groupBy(PeachGenotype::gene).map { (gene, genotypes) ->
            createPharmacoEntryForGeneAndPeachGenotypes(gene, genotypes)
        }.toSet()
    }

    private fun createPharmacoEntryForGeneAndPeachGenotypes(gene: String, peachGenotypes: List<PeachGenotype>): PharmacoEntry {
        return PharmacoEntry(
            gene = gene,
            haplotypes = peachGenotypes.map { Haplotype(name = it.haplotype(), function = it.function()) }.toSet()
        )
    }
}
