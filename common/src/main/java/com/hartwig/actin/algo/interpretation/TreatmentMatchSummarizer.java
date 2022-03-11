package com.hartwig.actin.algo.interpretation;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hartwig.actin.algo.datamodel.CohortEligibility;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.algo.datamodel.TrialEligibility;
import com.hartwig.actin.treatment.datamodel.CohortMetadata;
import com.hartwig.actin.treatment.datamodel.TrialIdentification;

import org.jetbrains.annotations.NotNull;

public final class TreatmentMatchSummarizer {

    private TreatmentMatchSummarizer() {
    }

    @NotNull
    public static TreatmentMatchSummary summarize(@NotNull TreatmentMatch treatmentMatch) {
        int trialCount = 0;
        int cohortCount = 0;

        Map<TrialIdentification, List<CohortMetadata>> eligibleTrialMap = Maps.newHashMap();
        for (TrialEligibility trial : treatmentMatch.trialMatches()) {
            trialCount++;
            // A trial without cohorts is considered a cohort on its own.
            boolean hasCohorts = !trial.cohorts().isEmpty();
            cohortCount += hasCohorts ? trial.cohorts().size() : 1;

            if (trial.isPotentiallyEligible()) {
                List<CohortMetadata> eligibleCohorts = Lists.newArrayList();
                for (CohortEligibility cohort : trial.cohorts()) {
                    if (cohort.isPotentiallyEligible()) {
                        eligibleCohorts.add(cohort.metadata());
                    }
                }

                eligibleTrialMap.put(trial.identification(), eligibleCohorts);
            }
        }

        return ImmutableTreatmentMatchSummary.builder()
                .trialCount(trialCount)
                .cohortCount(cohortCount)
                .eligibleTrialMap(eligibleTrialMap)
                .build();
    }
}
