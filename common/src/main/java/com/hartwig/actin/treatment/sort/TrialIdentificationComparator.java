package com.hartwig.actin.treatment.sort;

import java.util.Comparator;

import com.hartwig.actin.treatment.datamodel.TrialIdentification;

import org.jetbrains.annotations.NotNull;

public class TrialIdentificationComparator implements Comparator<TrialIdentification> {

    @Override
    public int compare(@NotNull TrialIdentification identification1, @NotNull TrialIdentification identification2) {
        int idCompare = identification1.trialId().compareTo(identification2.trialId());
        if (idCompare != 0) {
            return idCompare;
        }

        int acronymCompare = identification1.acronym().compareTo(identification2.acronym());
        if (acronymCompare != 0) {
            return acronymCompare;
        }

        int openCompare = Boolean.compare(identification2.open(), identification1.open());
        if (openCompare != 0) {
            return openCompare;
        }

        return identification1.title().compareTo(identification2.title());
    }
}
