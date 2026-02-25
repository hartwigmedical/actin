package com.hartwig.actin.molecular.findings

import com.hartwig.actin.datamodel.molecular.pharmaco.Haplotype
import com.hartwig.actin.datamodel.molecular.pharmaco.HaplotypeFunction
import com.hartwig.actin.datamodel.molecular.pharmaco.PharmacoEntry
import com.hartwig.actin.datamodel.molecular.pharmaco.PharmacoGene
import com.hartwig.hmftools.datamodel.finding.FindingList
import com.hartwig.hmftools.datamodel.finding.PharmocoGenotype

object PharmacoExtraction {

    fun extract(pharmocoGenoTypes: FindingList<PharmocoGenotype>): Set<PharmacoEntry> {
        return pharmocoGenoTypes.findings.groupBy(PharmocoGenotype::gene).map { (gene, genotypes) ->
            createPharmacoEntryForGeneAndPeachGenotypes(gene, genotypes)
        }.filterNot { it.gene == PharmacoGene.UGT1A1 }.toSet()
    }

    private fun createPharmacoEntryForGeneAndPeachGenotypes(gene: String, peachGenotypes: List<PharmocoGenotype>): PharmacoEntry {
        return PharmacoEntry(
            gene = determineGene(gene),
            haplotypes = peachGenotypes.map {
                Haplotype(
                    allele = it.allele(),
                    alleleCount = it.alleleCount(),
                    function = determineFunction(it.function())
                )
            }
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