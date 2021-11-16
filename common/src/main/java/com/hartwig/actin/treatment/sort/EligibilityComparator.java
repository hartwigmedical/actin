package com.hartwig.actin.treatment.sort;

import java.util.Comparator;

import com.hartwig.actin.treatment.datamodel.CriterionReference;
import com.hartwig.actin.treatment.datamodel.Eligibility;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;

import org.jetbrains.annotations.NotNull;

public class EligibilityComparator implements Comparator<Eligibility> {

    private static final Comparator<CriterionReference> CRITERION_COMPARATOR = new CriterionReferenceComparator();
    private static final Comparator<EligibilityFunction> FUNCTION_COMPARATOR = new EligibilityFunctionComparator();

    @Override
    public int compare(@NotNull Eligibility eligibility1, @NotNull Eligibility eligibility2) {
        if (eligibility1.references().isEmpty() && eligibility2.references().isEmpty()) {
            return FUNCTION_COMPARATOR.compare(eligibility1.function(), eligibility2.function());
        } else if (eligibility1.references().isEmpty()) {
            return 1;
        } else if (eligibility2.references().isEmpty()) {
            return -1;
        }

        CriterionReference reference1 = eligibility1.references().iterator().next();
        CriterionReference reference2 = eligibility2.references().iterator().next();
        int referenceCompare = CRITERION_COMPARATOR.compare(reference1, reference2);
        if (referenceCompare == 0) {
            return FUNCTION_COMPARATOR.compare(eligibility1.function(), eligibility2.function());
        } else {
            return referenceCompare;
        }
    }
}
