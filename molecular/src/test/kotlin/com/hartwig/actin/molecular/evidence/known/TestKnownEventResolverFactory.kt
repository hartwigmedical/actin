package com.hartwig.actin.molecular.evidence.known

import com.hartwig.serve.datamodel.ImmutableKnownEvents
import com.hartwig.serve.datamodel.KnownEvents

object TestKnownEventResolverFactory {

    fun createEmpty(): KnownEventResolver {
        return KnownEventResolver(ImmutableKnownEvents.builder().build(), emptySet())
    }

    fun createProper(): KnownEventResolver {
        val knownEvents: KnownEvents = ImmutableKnownEvents.builder()
            .addHotspots(TestServeKnownFactory.hotspotBuilder().build())
            .addCodons(TestServeKnownFactory.codonBuilder().build())
            .addExons(TestServeKnownFactory.exonBuilder().build())
            .addGenes(TestServeKnownFactory.geneBuilder().build())
            .addCopyNumbers(TestServeKnownFactory.copyNumberBuilder().build())
            .addFusions(TestServeKnownFactory.fusionBuilder().build())
            .build()

        return KnownEventResolver(knownEvents, knownEvents.genes())
    }
}
