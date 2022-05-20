package com.hartwig.actin.molecular.sort.evidence;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.evidence.TreatmentEvidence;

import org.jetbrains.annotations.NotNull;

public class TreatmentEvidenceComparator implements Comparator<TreatmentEvidence> {

    @Override
    public int compare(@NotNull TreatmentEvidence treatmentEvidence1, @NotNull TreatmentEvidence treatmentEvidence2) {
        int eventCompare = treatmentEvidence1.event().compareTo(treatmentEvidence2.event());
        if (eventCompare != 0) {
            return eventCompare;
        }

        return treatmentEvidence1.treatment().compareTo(treatmentEvidence2.treatment());
    }
}
