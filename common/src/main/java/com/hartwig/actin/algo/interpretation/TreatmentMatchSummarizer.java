package com.hartwig.actin.algo.interpretation;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.algo.datamodel.CohortEligibility;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.algo.datamodel.TrialEligibility;
import com.hartwig.actin.algo.util.EligibilityDisplay;

import org.jetbrains.annotations.NotNull;

public final class TreatmentMatchSummarizer {

    private TreatmentMatchSummarizer() {
    }

    @NotNull
    public static TreatmentMatchSummary summarize(@NotNull TreatmentMatch treatmentMatch) {
        int cohortCount = 0;

        Set<String> eligibleTrials = Sets.newHashSet();
        Set<String> eligibleCohorts = Sets.newHashSet();
        Set<String> eligibleOpenCohorts = Sets.newHashSet();
        for (TrialEligibility trial : treatmentMatch.trialMatches()) {
            // A trial without cohorts is considered a cohort on its own.
            boolean hasCohorts = !trial.cohorts().isEmpty();
            cohortCount += hasCohorts ? trial.cohorts().size() : 1;

            if (trial.overallEvaluation().isPass()) {
                boolean hasNoCohortOrAtLeastOneEligible = !hasCohorts;
                for (CohortEligibility cohort : trial.cohorts()) {
                    if (cohort.overallEvaluation().isPass()) {
                        eligibleCohorts.add(EligibilityDisplay.cohortName(trial, cohort));
                        hasNoCohortOrAtLeastOneEligible = true;
                        if (cohort.metadata().open()) {
                            eligibleOpenCohorts.add(EligibilityDisplay.cohortName(trial, cohort));
                        }
                    }
                }

                if (hasNoCohortOrAtLeastOneEligible) {
                    eligibleTrials.add(EligibilityDisplay.trialName(trial));

                    // Assume trials without specific cohorts are always open (or they should otherwise not exist
                    if (!hasCohorts) {
                        eligibleCohorts.add(EligibilityDisplay.trialName(trial));
                        eligibleOpenCohorts.add(EligibilityDisplay.trialName(trial));
                    }
                }
            }
        }

        return ImmutableTreatmentMatchSummary.builder()
                .trialCount(treatmentMatch.trialMatches().size())
                .eligibleTrials(eligibleTrials)
                .cohortCount(cohortCount)
                .eligibleCohorts(eligibleCohorts)
                .eligibleOpenCohorts(eligibleOpenCohorts)
                .build();
    }
}
