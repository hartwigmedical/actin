package com.hartwig.actin.molecular.sort.evidence;

import static org.junit.Assert.*;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableExternalTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableTreatmentEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.TreatmentEvidence;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class ExternalTrialEvidenceComparatorTest {

    @Test
    public void canSortExternalTrialEvidences() {
        ExternalTrialEvidence entry1 = create("event 3", "trial 5");
        ExternalTrialEvidence entry2 = create("event 3", "trial 4");
        ExternalTrialEvidence entry3 = create("event 2", "trial 6");

        List<ExternalTrialEvidence> entries = Lists.newArrayList(entry1, entry2, entry3);
        entries.sort(new ExternalTrialEvidenceComparator());

        assertEquals(entry3, entries.get(0));
        assertEquals(entry2, entries.get(1));
        assertEquals(entry1, entries.get(2));
    }

    @NotNull
    private static ExternalTrialEvidence create(@NotNull String event, @NotNull String trial) {
        return ImmutableExternalTrialEvidence.builder().event(event).trial(trial).build();
    }

}