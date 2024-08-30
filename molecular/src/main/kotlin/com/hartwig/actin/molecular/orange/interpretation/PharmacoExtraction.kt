package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.datamodel.molecular.orange.pharmaco.Haplotype
import com.hartwig.actin.datamodel.molecular.orange.pharmaco.HaplotypeFunction
import com.hartwig.actin.datamodel.molecular.orange.pharmaco.PharmacoEntry
import com.hartwig.actin.datamodel.molecular.orange.pharmaco.PharmacoGene
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
            gene = determineGene(gene),
            haplotypes = peachGenotypes.map { Haplotype(allele = it.allele(), alleleCount = it.alleleCount(), function = determineFunction(it.function())) }
                .toSet()
        )
    }

    private fun determineGene(gene: String): PharmacoGene {
        try {
            return PharmacoGene.valueOf(gene.uppercase())
        } catch (e: Exception) {
            throw IllegalStateException("Unexpected pharmaco gene: $gene ")
        }
    }

    private fun determineFunction(function: String): HaplotypeFunction {
        try {
            return HaplotypeFunction.valueOf(function.trim { it <= ' ' }.replace(" ".toRegex(), "_").uppercase())
        } catch (e: Exception) {
            throw IllegalStateException("Unexpected haplotype function: $function ")
        }
    }
}