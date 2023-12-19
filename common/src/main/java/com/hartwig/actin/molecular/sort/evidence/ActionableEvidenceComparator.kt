package com.hartwig.actin.molecular.sort.evidence;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence;

import org.jetbrains.annotations.NotNull;

public class ActionableEvidenceComparator implements Comparator<ActionableEvidence> {

    @Override
    public int compare(@NotNull ActionableEvidence evidence1, @NotNull ActionableEvidence evidence2) {
        int approvedCompare = Integer.compare(evidence2.approvedTreatments().size(), evidence1.approvedTreatments().size());
        if (approvedCompare != 0) {
            return approvedCompare;
        }

        int externalTrialCompare = Integer.compare(evidence2.externalEligibleTrials().size(), evidence1.externalEligibleTrials().size());
        if (externalTrialCompare != 0) {
            return externalTrialCompare;
        }

        int onLabelCompare =
                Integer.compare(evidence2.onLabelExperimentalTreatments().size(), evidence1.onLabelExperimentalTreatments().size());
        if (onLabelCompare != 0) {
            return onLabelCompare;
        }

        int offLabelCompare =
                Integer.compare(evidence2.offLabelExperimentalTreatments().size(), evidence1.offLabelExperimentalTreatments().size());
        if (offLabelCompare != 0) {
            return offLabelCompare;
        }

        int preClinicalCompare = Integer.compare(evidence2.preClinicalTreatments().size(), evidence1.preClinicalTreatments().size());
        if (preClinicalCompare != 0) {
            return preClinicalCompare;
        }

        int knownResistantCompare =
                Integer.compare(evidence2.knownResistantTreatments().size(), evidence1.knownResistantTreatments().size());
        if (knownResistantCompare != 0) {
            return knownResistantCompare;
        }

        return Integer.compare(evidence2.suspectResistantTreatments().size(), evidence1.suspectResistantTreatments().size());
    }
}
