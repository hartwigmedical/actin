package com.hartwig.actin.molecular.evidence.known

import com.hartwig.serve.datamodel.molecular.ImmutableKnownEvents

object TestKnownEventResolverFactory {

    fun createProper(): KnownEventResolver {
        val primaryKnownEvents = ImmutableKnownEvents.builder()
            .addHotspots(TestServeKnownFactory.hotspotBuilder().build())
            .addCodons(TestServeKnownFactory.codonBuilder().build())
            .addExons(TestServeKnownFactory.exonBuilder().build())
            .addGenes(TestServeKnownFactory.geneBuilder().build())
            .addCopyNumbers(TestServeKnownFactory.copyNumberBuilder().build())
            .addFusions(TestServeKnownFactory.fusionBuilder().build())
            .build()

        val secondaryKnownEvents = ImmutableKnownEvents.builder().build()

        return KnownEventResolver(primaryKnownEvents, secondaryKnownEvents, primaryKnownEvents.genes())
    }
}
