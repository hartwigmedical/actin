package com.hartwig.actin.molecular.sort.evidence;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.evidence.TreatmentEvidence;

import org.jetbrains.annotations.NotNull;

public class TreatmentEvidenceComparator implements Comparator<TreatmentEvidence> {

    @Override
    public int compare(@NotNull TreatmentEvidence evidence1, @NotNull TreatmentEvidence evidence2) {
        int eventCompare = evidence1.event().compareTo(evidence2.event());
        if (eventCompare != 0) {
            return eventCompare;
        }

        return evidence1.treatment().compareTo(evidence2.treatment());
    }
}
