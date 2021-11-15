package com.hartwig.actin.treatment.sort;

import java.util.Comparator;

import com.hartwig.actin.treatment.datamodel.CriterionReference;
import com.hartwig.actin.treatment.datamodel.Eligibility;

import org.jetbrains.annotations.NotNull;

public class EligibilityComparator implements Comparator<Eligibility> {

    private static final Comparator<CriterionReference> CRITERION_COMPARATOR = new CriterionReferenceComparator();

    @Override
    public int compare(@NotNull Eligibility eligibility1, @NotNull Eligibility eligibility2) {
        if (eligibility1.references().isEmpty()) {
            return -1;
        } else if (eligibility2.references().isEmpty()) {
            return 1;
        } else {
            return CRITERION_COMPARATOR.compare(eligibility1.references().iterator().next(), eligibility2.references().iterator().next());
        }
    }
}
