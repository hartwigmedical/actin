package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.datamodel.orange.pharmaco.Haplotype
import com.hartwig.actin.molecular.datamodel.orange.pharmaco.PharmacoEntry
import com.hartwig.hmftools.datamodel.orange.OrangeRecord
import com.hartwig.hmftools.datamodel.peach.PeachGenotype

internal object PharmacoExtraction {

    private val expectedHaplotypeFunctions = setOf("normal function", "reduced function", "no function")
    private val pharmacoGenes = setOf("DPYD", "UGT1A1")

    fun extract(record: OrangeRecord): Set<PharmacoEntry> {
        val peach = record.peach() ?: return emptySet()
        return peach.groupBy(PeachGenotype::gene).map { (gene, genotypes) ->
            if (gene !in pharmacoGenes) throw IllegalStateException("Unexpected pharmaco gene: $gene")
            genotypes.forEach { function ->
                val functionName = function.function().lowercase()
                if (functionName !in expectedHaplotypeFunctions) {
                    throw IllegalStateException("Unexpected haplotype function: $functionName")
                }
            }
            createPharmacoEntryForGeneAndPeachGenotypes(gene, genotypes)
        }.toSet()
    }

    private fun createPharmacoEntryForGeneAndPeachGenotypes(gene: String, peachGenotypes: List<PeachGenotype>): PharmacoEntry {
        return PharmacoEntry(
            gene = gene,
            haplotypes = peachGenotypes.map { Haplotype(allele = it.allele(), alleleCount = it.alleleCount(), function = it.function()) }
                .toSet()
        )
    }
}