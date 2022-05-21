package com.hartwig.actin.molecular.orange.interpretation;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
        Map<String, List<PeachEntry>> peachEntryPerGene = Maps.newHashMap();

        for (PeachEntry entry : record.peach().entries()) {
            List<PeachEntry> entries = peachEntryPerGene.get(entry.gene());
            if (entries == null) {
                entries = Lists.newArrayList();
            }
            entries.add(entry);
            peachEntryPerGene.put(entry.gene(), entries);
        }

        Set<PharmacoEntry> entries = Sets.newHashSet();
        for (Map.Entry<String, List<PeachEntry>> mapEntry : peachEntryPerGene.entrySet()) {
            Set<Haplotype> haplotypes = Sets.newHashSet();
            for (PeachEntry entry : mapEntry.getValue()) {
                haplotypes.add(ImmutableHaplotype.builder().name(entry.haplotype()).function(entry.function()).build());
            }
            entries.add(ImmutablePharmacoEntry.builder().gene(mapEntry.getKey()).haplotypes(haplotypes).build());
        }
        return entries;
    }
}
