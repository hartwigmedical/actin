package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.datamodel.pharmaco.ImmutableHaplotype
import com.hartwig.actin.molecular.datamodel.pharmaco.ImmutablePharmacoEntry
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry
import com.hartwig.hmftools.datamodel.orange.OrangeRecord
import com.hartwig.hmftools.datamodel.peach.PeachGenotype
import java.util.stream.Collectors

internal object PharmacoExtraction {
    fun extract(record: OrangeRecord): Set<PharmacoEntry> {
        val peach = record.peach()
        return if (peach != null) {
            peach.groupBy(PeachGenotype::gene).map { (gene, genotypes) ->
                createPharmacoEntryForGeneAndPeachGenotypes(gene, genotypes)
            }.toSet()
        } else {
            setOf()
        }
    }

    private fun createPharmacoEntryForGeneAndPeachGenotypes(gene: String, peachGenotypes: List<PeachGenotype>): PharmacoEntry {
        val haplotypes = peachGenotypes.stream()
            .map { peachGenotype: PeachGenotype ->
                ImmutableHaplotype.builder()
                    .name(peachGenotype.haplotype())
                    .function(peachGenotype.function())
                    .build()
            }
            .collect(Collectors.toSet())
        return ImmutablePharmacoEntry.builder().gene(gene).haplotypes(haplotypes).build()
    }
}
