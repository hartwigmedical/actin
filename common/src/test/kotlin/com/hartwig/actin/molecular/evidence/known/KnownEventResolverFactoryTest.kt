package com.hartwig.actin.molecular.evidence.known

import com.hartwig.actin.molecular.evidence.known.KnownEventResolverFactory.PRIMARY_KNOWN_EVENT_SOURCE
import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.molecular.ImmutableKnownEvents
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class KnownEventResolverFactoryTest {

    @Test
    fun `Should create from empty resources`() {
        assertThat(KnownEventResolverFactory.create(ImmutableKnownEvents.builder().build())).isNotNull
    }

    @Test
    fun `Should filter known events`() {
        val hotspot1 = TestServeKnownFactory.hotspotBuilder().addSources(PRIMARY_KNOWN_EVENT_SOURCE, Knowledgebase.UNKNOWN).build()
        val hotspot2 = TestServeKnownFactory.hotspotBuilder().addSources(PRIMARY_KNOWN_EVENT_SOURCE).build()
        val hotspot3 = TestServeKnownFactory.hotspotBuilder().addSources(Knowledgebase.UNKNOWN).build()
        val knownEvents = ImmutableKnownEvents.builder().addHotspots(hotspot1, hotspot2, hotspot3).build()

        val primaryKnownEvents = KnownEventResolverFactory.includeKnownEvents(knownEvents, true)
        assertThat(primaryKnownEvents.hotspots()).containsExactlyInAnyOrder(hotspot1, hotspot2)

        val secondaryKnownEvents = KnownEventResolverFactory.includeKnownEvents(knownEvents, false)
        assertThat(secondaryKnownEvents.hotspots()).containsExactlyInAnyOrder(hotspot3)
    }
}