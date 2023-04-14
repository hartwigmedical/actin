package com.hartwig.actin.molecular.orange.interpretation;

import static java.util.stream.Collectors.groupingBy;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.hartwig.actin.molecular.datamodel.pharmaco.Haplotype;
import com.hartwig.actin.molecular.datamodel.pharmaco.ImmutableHaplotype;
import com.hartwig.actin.molecular.datamodel.pharmaco.ImmutablePharmacoEntry;
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.peach.PeachEntry;

import org.jetbrains.annotations.NotNull;

final class PharmacoExtraction {

    private PharmacoExtraction() {
    }

    @NotNull
    public static Set<PharmacoEntry> extract(@NotNull OrangeRecord record) {
        return record.peach()
                .map(peachRecord -> peachRecord.entries()
                        .stream()
                        .collect(groupingBy(PeachEntry::gene))
                        .entrySet()
                        .stream()
                        .map(geneWithPeachEntries -> createPharmacoEntryForGeneAndPeachEntries(geneWithPeachEntries.getKey(),
                                geneWithPeachEntries.getValue()))
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }

    @NotNull
    private static PharmacoEntry createPharmacoEntryForGeneAndPeachEntries(String gene, List<PeachEntry> peachEntries) {
        Set<Haplotype> haplotypes = peachEntries.stream()
                .map(peachEntry -> ImmutableHaplotype.builder().name(peachEntry.haplotype()).function(peachEntry.function()).build())
                .collect(Collectors.toSet());
        return ImmutablePharmacoEntry.builder().gene(gene).haplotypes(haplotypes).build();
    }
}
