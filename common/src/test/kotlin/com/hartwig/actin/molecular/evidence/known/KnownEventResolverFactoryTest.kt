package com.hartwig.actin.molecular.evidence.known

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
        val hotspot1 = TestServeKnownFactory.hotspotBuilder().addSources(firstAllowedSource(), Knowledgebase.UNKNOWN).build()
        val hotspot2 = TestServeKnownFactory.hotspotBuilder().addSources(firstAllowedSource()).build()
        val hotspot3 = TestServeKnownFactory.hotspotBuilder().addSources(Knowledgebase.UNKNOWN).build()
        val knownEvents = ImmutableKnownEvents.builder().addHotspots(hotspot1, hotspot2, hotspot3).build()

        val filtered = KnownEventResolverFactory.filterKnownEvents(knownEvents)
        assertThat(filtered.hotspots().size.toLong()).isEqualTo(2)
        assertThat(filtered.hotspots().contains(hotspot1)).isTrue()
        assertThat(filtered.hotspots().contains(hotspot2)).isTrue()
    }

    private fun firstAllowedSource(): Knowledgebase {
        return KnownEventResolverFactory.KNOWN_EVENT_SOURCES.iterator().next()
    }
}