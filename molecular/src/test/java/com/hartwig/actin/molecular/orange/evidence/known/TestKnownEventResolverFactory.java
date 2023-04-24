package com.hartwig.actin.molecular.orange.evidence.known;

import com.hartwig.serve.datamodel.ImmutableKnownEvents;
import com.hartwig.serve.datamodel.KnownEvents;

import org.jetbrains.annotations.NotNull;

public final class TestKnownEventResolverFactory {

    private TestKnownEventResolverFactory() {
    }

    @NotNull
    public static KnownEventResolver createEmpty() {
        return new KnownEventResolver(ImmutableKnownEvents.builder().build());
    }

    @NotNull
    public static KnownEventResolver createProper() {
        KnownEvents knownEvents = ImmutableKnownEvents.builder()
                .addHotspots(TestServeKnownFactory.hotspotBuilder().build())
                .addCodons(TestServeKnownFactory.codonBuilder().build())
                .addExons(TestServeKnownFactory.exonBuilder().build())
                .addGenes(TestServeKnownFactory.geneBuilder().build())
                .addCopyNumbers(TestServeKnownFactory.copyNumberBuilder().build())
                .addFusions(TestServeKnownFactory.fusionBuilder().build())
                .build();

        return new KnownEventResolver(knownEvents);
    }
}
