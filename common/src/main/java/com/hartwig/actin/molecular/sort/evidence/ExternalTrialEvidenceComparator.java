package com.hartwig.actin.molecular.sort.evidence;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrialEvidence;

import org.jetbrains.annotations.NotNull;

public class ExternalTrialEvidenceComparator implements Comparator<ExternalTrialEvidence> {

    @Override
    public int compare(@NotNull ExternalTrialEvidence evidence1, @NotNull ExternalTrialEvidence evidence2) {
        int eventCompare = evidence1.event().compareTo(evidence2.event());
        if (eventCompare != 0) {
            return eventCompare;
        }

        return evidence1.trial().compareTo(evidence2.trial());
    }
}
