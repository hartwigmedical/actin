package com.hartwig.actin.molecular.orange.evidence.known

import com.hartwig.serve.datamodel.ImmutableKnownEvents
import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.KnownEvents
import com.hartwig.serve.datamodel.hotspot.KnownHotspot
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KnownEventResolverFactoryTest {

    @Test
    fun canCreateFromEmptyInputs() {
        Assert.assertNotNull(KnownEventResolverFactory.create(ImmutableKnownEvents.builder().build()))
    }

    @Test
    fun canFilterKnownEvents() {
        val hotspot1: KnownHotspot = TestServeKnownFactory.hotspotBuilder().addSources(firstAllowedSource(), Knowledgebase.UNKNOWN).build()
        val hotspot2: KnownHotspot = TestServeKnownFactory.hotspotBuilder().addSources(firstAllowedSource()).build()
        val hotspot3: KnownHotspot = TestServeKnownFactory.hotspotBuilder().addSources(Knowledgebase.UNKNOWN).build()
        val known: KnownEvents = ImmutableKnownEvents.builder().addHotspots(hotspot1, hotspot2, hotspot3).build()

        val filtered = KnownEventResolverFactory.filterKnownEvents(known)
        assertEquals(2, filtered.hotspots().size.toLong())
        assertTrue(filtered.hotspots().contains(hotspot1))
        assertTrue(filtered.hotspots().contains(hotspot2))
    }

    companion object {
        private fun firstAllowedSource(): Knowledgebase {
            return KnownEventResolverFactory.KNOWN_EVENT_SOURCES.iterator().next()
        }
    }
}