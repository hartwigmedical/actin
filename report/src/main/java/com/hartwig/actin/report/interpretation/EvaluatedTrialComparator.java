package com.hartwig.actin.report.interpretation;

import java.util.Comparator;

import org.jetbrains.annotations.NotNull;

public class EvaluatedTrialComparator implements Comparator<EvaluatedTrial> {

    @Override
    public int compare(@NotNull EvaluatedTrial trial1, @NotNull EvaluatedTrial trial2) {
        int hasMolecularCompare = Boolean.compare(trial2.hasMolecularEvidence(), trial1.hasMolecularEvidence());
        if (hasMolecularCompare != 0) {
            return hasMolecularCompare;
        }

        int trialCompare = trial1.trialId().compareTo(trial2.trialId());
        if (trialCompare != 0) {
            return trialCompare;
        }

        if (trial1.cohort() == null) {
            return trial2.cohort() != null ? -1 : 0;
        } else {
            return trial1.cohort().compareTo(trial2.cohort());
        }
    }
}
