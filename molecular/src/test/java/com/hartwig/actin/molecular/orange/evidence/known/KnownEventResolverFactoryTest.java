package com.hartwig.actin.molecular.orange.evidence.known;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.hartwig.serve.datamodel.ImmutableKnownEvents;
import com.hartwig.serve.datamodel.Knowledgebase;
import com.hartwig.serve.datamodel.KnownEvents;
import com.hartwig.serve.datamodel.hotspot.KnownHotspot;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class KnownEventResolverFactoryTest {

    @Test
    public void canCreateFromEmptyInputs() {
        assertNotNull(KnownEventResolverFactory.create(ImmutableKnownEvents.builder().build(), Lists.newArrayList()));
    }

    @Test
    public void canFilterKnownEvents() {
        KnownHotspot hotspot1 = TestServeKnownFactory.hotspotBuilder().addSources(firstAllowedSource(), Knowledgebase.UNKNOWN).build();
        KnownHotspot hotspot2 = TestServeKnownFactory.hotspotBuilder().addSources(firstAllowedSource()).build();
        KnownHotspot hotspot3 = TestServeKnownFactory.hotspotBuilder().addSources(Knowledgebase.UNKNOWN).build();

        KnownEvents known = ImmutableKnownEvents.builder().addHotspots(hotspot1, hotspot2, hotspot3).build();

        KnownEvents filtered = KnownEventResolverFactory.filterKnownEvents(known);
        assertEquals(2, filtered.hotspots().size());
        assertTrue(filtered.hotspots().contains(hotspot1));
        assertTrue(filtered.hotspots().contains(hotspot2));
    }

    @NotNull
    private static Knowledgebase firstAllowedSource() {
        return KnownEventResolverFactory.KNOWN_EVENT_SOURCES.iterator().next();
    }
}