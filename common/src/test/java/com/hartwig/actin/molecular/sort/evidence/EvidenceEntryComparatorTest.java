package com.hartwig.actin.molecular.sort.evidence;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceEntry;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableEvidenceEntry;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class EvidenceEntryComparatorTest {

    @Test
    public void canSortEvidenceEntries() {
        EvidenceEntry entry1 = create("event 3", "treatment 5");
        EvidenceEntry entry2 = create("event 3", "treatment 4");
        EvidenceEntry entry3 = create("event 2", "treatment 6");

        List<EvidenceEntry> entries = Lists.newArrayList(entry1, entry2, entry3);
        entries.sort(new EvidenceEntryComparator());

        assertEquals(entry3, entries.get(0));
        assertEquals(entry2, entries.get(1));
        assertEquals(entry1, entries.get(2));
    }

    @NotNull
    private static EvidenceEntry create(@NotNull String event, @NotNull String treatment) {
        return ImmutableEvidenceEntry.builder().event(event).treatment(treatment).build();
    }
}