package com.hartwig.actin.molecular.orange.evidence.known;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.serve.KnownGene;
import com.hartwig.actin.molecular.serve.TestKnownGeneFactory;
import com.hartwig.serve.datamodel.ImmutableKnownEvents;
import com.hartwig.serve.datamodel.KnownEvents;

import org.jetbrains.annotations.NotNull;

public final class TestKnownEventResolverFactory {

    private TestKnownEventResolverFactory() {
    }

    @NotNull
    public static KnownEventResolver createEmpty() {
        return new KnownEventResolver(ImmutableKnownEvents.builder().build(), Lists.newArrayList());
    }

    @NotNull
    public static KnownEventResolver createProper() {
        KnownEvents knownEvents = ImmutableKnownEvents.builder()
                .addHotspots(TestServeKnownFactory.hotspotBuilder().build())
                .addCodons(TestServeKnownFactory.codonBuilder().build())
                .addExons(TestServeKnownFactory.exonBuilder().build())
                .addCopyNumbers(TestServeKnownFactory.copyNumberBuilder().build())
                .addFusions(TestServeKnownFactory.fusionBuilder().build())
                .build();

        List<KnownGene> knownGenes = Lists.newArrayList();
        knownGenes.add(TestKnownGeneFactory.builder().build());

        return new KnownEventResolver(knownEvents, knownGenes);
    }
}
