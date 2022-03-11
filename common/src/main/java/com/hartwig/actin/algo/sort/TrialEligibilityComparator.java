package com.hartwig.actin.algo.sort;

import java.util.Comparator;

import com.hartwig.actin.algo.datamodel.CohortEligibility;
import com.hartwig.actin.algo.datamodel.TrialEligibility;
import com.hartwig.actin.treatment.datamodel.TrialIdentification;
import com.hartwig.actin.treatment.sort.TrialIdentificationComparator;

import org.jetbrains.annotations.NotNull;

public class TrialEligibilityComparator implements Comparator<TrialEligibility> {

    private static final Comparator<TrialIdentification> IDENTIFICATION_COMPARATOR = new TrialIdentificationComparator();
    private static final Comparator<CohortEligibility> COHORT_ELIGIBILITY_COMPARATOR = new CohortEligibilityComparator();

    @Override
    public int compare(@NotNull TrialEligibility eligibility1, @NotNull TrialEligibility eligibility2) {
        int identificationCompare = IDENTIFICATION_COMPARATOR.compare(eligibility1.identification(), eligibility2.identification());
        if (identificationCompare != 0) {
            return identificationCompare;
        }

        int isPotentiallyEligibleCompare = Boolean.compare(eligibility1.isPotentiallyEligible(), eligibility2.isPotentiallyEligible());
        if (isPotentiallyEligibleCompare != 0) {
            return isPotentiallyEligibleCompare;
        }

        int sizeCompare = eligibility1.cohorts().size() - eligibility2.cohorts().size();
        if (sizeCompare != 0) {
            return sizeCompare > 0 ? 1 : -1;
        }

        int index = 0;
        while (index < eligibility1.cohorts().size()) {
            int cohortCompare = COHORT_ELIGIBILITY_COMPARATOR.compare(eligibility1.cohorts().get(index), eligibility2.cohorts().get(index));
            if (cohortCompare != 0) {
                return cohortCompare;
            }
            index++;
        }

        return EvaluationMapCompareUtil.compareEvaluationMaps(eligibility1.evaluations(), eligibility2.evaluations());
    }
}
