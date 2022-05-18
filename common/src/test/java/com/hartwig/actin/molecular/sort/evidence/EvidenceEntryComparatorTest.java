package com.hartwig.actin.molecular.sort.evidence;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceEntry;
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceType;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableEvidenceEntry;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class EvidenceEntryComparatorTest {

    @Test
    public void canSortEvidenceEntries() {
        EvidenceEntry entry1 = create("event 3", "source event 3", EvidenceType.SIGNATURE, "treatment 5");
        EvidenceEntry entry2 = create("event 3", "source event 3", EvidenceType.HOTSPOT_MUTATION, "treatment 5");
        EvidenceEntry entry3 = create("event 3", "source event 3", EvidenceType.WILD_TYPE, "treatment 4");
        EvidenceEntry entry4 = create("event 2", "source event 2", EvidenceType.HOTSPOT_MUTATION, "treatment 6");

        List<EvidenceEntry> entries = Lists.newArrayList(entry1, entry2, entry3, entry4);
        entries.sort(new EvidenceEntryComparator());

        assertEquals(entry4, entries.get(0));
        assertEquals(entry3, entries.get(1));
        assertEquals(entry1, entries.get(2));
        assertEquals(entry2, entries.get(3));
    }

    @NotNull
    private static EvidenceEntry create(@NotNull String event, @NotNull String sourceEvent, @NotNull EvidenceType sourceType,
            @NotNull String treatment) {
        return ImmutableEvidenceEntry.builder().event(event).sourceEvent(sourceEvent).sourceType(sourceType).treatment(treatment).build();
    }
}