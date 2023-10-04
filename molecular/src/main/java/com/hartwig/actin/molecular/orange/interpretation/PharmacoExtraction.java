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
import com.hartwig.hmftools.datamodel.orange.OrangeRecord;
import com.hartwig.hmftools.datamodel.peach.PeachGenotype;

import org.jetbrains.annotations.NotNull;

final class PharmacoExtraction {

    private PharmacoExtraction() {
    }

    @NotNull
    public static Set<PharmacoEntry> extract(@NotNull OrangeRecord record) {
        Set<PeachGenotype> peach = record.peach();
        if (peach != null) {
            return peach.stream().collect(groupingBy(PeachGenotype::gene))
                    .entrySet()
                    .stream()
                    .map(x -> createPharmacoEntryForGeneAndPeachGenotypes(x.getKey(), x.getValue()))
                    .collect(Collectors.toSet());
        } else {
            return Collections.emptySet();
        }
    }

    @NotNull
    private static PharmacoEntry createPharmacoEntryForGeneAndPeachGenotypes(String gene, List<PeachGenotype> peachGenotypes) {
        Set<Haplotype> haplotypes = peachGenotypes.stream()
                .map(peachGenotype -> ImmutableHaplotype.builder()
                        .name(peachGenotype.haplotype())
                        .function(peachGenotype.function())
                        .build())
                .collect(Collectors.toSet());
        return ImmutablePharmacoEntry.builder().gene(gene).haplotypes(haplotypes).build();
    }
}
