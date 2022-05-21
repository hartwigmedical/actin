package com.hartwig.actin.molecular.sort.evidence;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableTreatmentEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.TreatmentEvidence;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class TreatmentEvidenceComparatorTest {

    @Test
    public void canSortTreatmentEvidences() {
        TreatmentEvidence entry1 = create("event 3", "treatment 5");
        TreatmentEvidence entry2 = create("event 3", "treatment 4");
        TreatmentEvidence entry3 = create("event 2", "treatment 6");

        List<TreatmentEvidence> entries = Lists.newArrayList(entry1, entry2, entry3);
        entries.sort(new TreatmentEvidenceComparator());

        assertEquals(entry3, entries.get(0));
        assertEquals(entry2, entries.get(1));
        assertEquals(entry1, entries.get(2));
    }

    @NotNull
    private static TreatmentEvidence create(@NotNull String event, @NotNull String treatment) {
        return ImmutableTreatmentEvidence.builder().event(event).treatment(treatment).build();
    }
}