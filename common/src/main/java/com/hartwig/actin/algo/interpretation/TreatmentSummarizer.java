package com.hartwig.actin.algo.interpretation;

import com.hartwig.actin.algo.datamodel.CohortEligibility;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.algo.datamodel.TrialEligibility;

import org.jetbrains.annotations.NotNull;

public final class TreatmentSummarizer {

    private TreatmentSummarizer() {
    }

    @NotNull
    public static TreatmentSummary summarize(@NotNull TreatmentMatch treatmentMatch) {
        int eligibleTrialCount = 0;
        int cohortCount = 0;
        int eligibleCohortCount = 0;
        int eligibleOpenCohortCount = 0;

        for (TrialEligibility trial : treatmentMatch.trialMatches()) {
            // A trial without cohorts is considered a cohort on its own.
            boolean hasCohorts = !trial.cohorts().isEmpty();
            cohortCount += hasCohorts ? trial.cohorts().size() : 1;

            if (trial.overallEvaluation().isPass()) {
                boolean hasNoCohortOrAtLeastOneEligible = !hasCohorts;
                for (CohortEligibility cohort : trial.cohorts()) {
                    if (cohort.overallEvaluation().isPass()) {
                        eligibleCohortCount += 1;
                        hasNoCohortOrAtLeastOneEligible = true;
                        if (cohort.metadata().open()) {
                            eligibleOpenCohortCount += 1;
                        }
                    }
                }

                if (hasNoCohortOrAtLeastOneEligible) {
                    eligibleTrialCount += 1;

                    if (!hasCohorts) {
                        eligibleOpenCohortCount += 1;
                    }
                }
            }
        }

        return ImmutableTreatmentSummary.builder()
                .trialCount(treatmentMatch.trialMatches().size())
                .eligibleTrialCount(eligibleTrialCount)
                .cohortCount(cohortCount)
                .eligibleCohortCount(eligibleCohortCount)
                .eligibleOpenCohortCount(eligibleOpenCohortCount)
                .build();
    }
}
