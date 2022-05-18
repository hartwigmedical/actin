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

        int treatmentCompare = evidenceEntry1.treatment().compareTo(evidenceEntry2.treatment());
        if (treatmentCompare != 0) {
            return treatmentCompare;
        }

        int sourceEventCompare = evidenceEntry1.sourceEvent().compareTo(evidenceEntry2.sourceEvent());
        if (sourceEventCompare != 0) {
            return sourceEventCompare;
        }

        return evidenceEntry1.sourceType().compareTo(evidenceEntry2.sourceType());
    }
}
