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
            haplotypes = peachGenotypes.map { Haplotype(name = toDisplayName(it), function = it.function()) }.toSet()
        )
    }

    private fun toDisplayName(peachGenotype: PeachGenotype): String {
        if (peachGenotype.alleleCount() < 1 || peachGenotype.alleleCount() > 2) {
            throw IllegalArgumentException("Invalid Peach allele count, expected 1 or 2: ${peachGenotype.alleleCount()}")
        }

        return "${peachGenotype.allele()} ${if (peachGenotype.alleleCount() < 2) "HET" else "HOM"}"
    }
}