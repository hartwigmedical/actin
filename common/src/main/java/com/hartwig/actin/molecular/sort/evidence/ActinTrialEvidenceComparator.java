package com.hartwig.actin.molecular.sort.evidence;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.evidence.ActinTrialEvidence;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ActinTrialEvidenceComparator implements Comparator<ActinTrialEvidence> {

    @Override
    public int compare(@NotNull ActinTrialEvidence evidence1, @NotNull ActinTrialEvidence evidence2) {
        int eventCompare = evidence1.event().compareTo(evidence2.event());
        if (eventCompare != 0) {
            return eventCompare;
        }

        int trialAcronymCompare = evidence1.trialAcronym().compareTo(evidence2.trialAcronym());
        if (trialAcronymCompare != 0) {
            return trialAcronymCompare;
        }

        int cohortIdCompare = compareStrings(evidence1.cohortId(), evidence2.cohortId());
        if (cohortIdCompare != 0) {
            return cohortIdCompare;
        }

        int isInclusionCriterionCompare = Boolean.compare(evidence1.isInclusionCriterion(), evidence2.isInclusionCriterion());
        if (isInclusionCriterionCompare != 0) {
            return isInclusionCriterionCompare;
        }

        int typeCompare = evidence1.rule().compareTo(evidence2.rule());
        if (typeCompare != 0) {
            return typeCompare;
        }

        int geneCompare = compareStrings(evidence1.gene(), evidence2.gene());
        if (geneCompare != 0) {
            return geneCompare;
        }

        return compareStrings(evidence1.mutation(), evidence2.mutation());
    }

    private int compareStrings(@Nullable String string1, @Nullable String string2) {
        if (string1 == null && string2 == null) {
            return 0;
        } else if (string1 == null) {
            return 1;
        } else if (string2 == null) {
            return -1;
        } else {
            return string1.compareTo(string2);
        }
    }
}
