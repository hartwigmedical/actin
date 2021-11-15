package com.hartwig.actin.treatment.sort;

import java.util.Comparator;

import com.hartwig.actin.treatment.datamodel.CriterionReference;

import org.jetbrains.annotations.NotNull;

public class CriterionReferenceComparator implements Comparator<CriterionReference> {

    @Override
    public int compare(@NotNull CriterionReference reference1, @NotNull CriterionReference reference2) {
        boolean ref1Preferred = reference1.id().startsWith("I");
        boolean ref2Preferred = reference2.id().startsWith("I");

        if (ref1Preferred && !ref2Preferred) {
            return -1;
        } else if (!ref1Preferred && ref2Preferred) {
            return 1;
        } else {
            return reference1.id().compareTo(reference2.id());
        }
    }
}
