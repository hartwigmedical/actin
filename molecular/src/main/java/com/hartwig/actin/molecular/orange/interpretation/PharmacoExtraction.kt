package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.datamodel.pharmaco.ImmutableHaplotype
import com.hartwig.actin.molecular.datamodel.pharmaco.ImmutablePharmacoEntry
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry
import com.hartwig.hmftools.datamodel.orange.OrangeRecord
import com.hartwig.hmftools.datamodel.peach.PeachGenotype
import java.util.function.Function
import java.util.stream.Collectors

internal object PharmacoExtraction {
    fun extract(record: OrangeRecord): MutableSet<PharmacoEntry?> {
        val peach = record.peach()
        return if (peach != null) {
            peach.stream()
                .collect(Collectors.groupingBy(Function { obj: PeachGenotype? -> obj.gene() }))
                .entries
                .stream()
                .map { x: MutableMap.MutableEntry<String?, MutableList<PeachGenotype?>?>? -> createPharmacoEntryForGeneAndPeachGenotypes(x.key, x.value) }
                .collect(Collectors.toSet())
        } else {
            emptySet<PharmacoEntry?>()
        }
    }

    private fun createPharmacoEntryForGeneAndPeachGenotypes(gene: String?, peachGenotypes: MutableList<PeachGenotype?>?): PharmacoEntry {
        val haplotypes = peachGenotypes.stream()
            .map { peachGenotype: PeachGenotype? ->
                ImmutableHaplotype.builder()
                    .name(peachGenotype.haplotype())
                    .function(peachGenotype.function())
                    .build()
            }
            .collect(Collectors.toSet())
        return ImmutablePharmacoEntry.builder().gene(gene).haplotypes(haplotypes).build()
    }
}
