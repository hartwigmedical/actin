package com.hartwig.actin.molecular.evidence.known

import com.hartwig.serve.datamodel.molecular.ImmutableKnownEvents
import com.hartwig.serve.datamodel.molecular.KnownEvents

object TestKnownEventResolverFactory {

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
