package com.hartwig.actin.molecular.sort.evidence;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.evidence.EvidenceEntry;

import org.jetbrains.annotations.NotNull;

public class EvidenceEntryComparator implements Comparator<EvidenceEntry> {

    @Override
    public int compare(@NotNull EvidenceEntry evidenceEntry1, @NotNull EvidenceEntry evidenceEntry2) {
        int eventCompare = evidenceEntry1.event().compareTo(evidenceEntry2.event());
        if (eventCompare != 0) {
            return eventCompare;
        }

        return evidenceEntry1.treatment().compareTo(evidenceEntry2.treatment());
    }
}
