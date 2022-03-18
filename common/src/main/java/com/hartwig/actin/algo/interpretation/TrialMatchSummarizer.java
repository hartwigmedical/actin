package com.hartwig.actin.algo.interpretation;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hartwig.actin.algo.datamodel.CohortMatch;
import com.hartwig.actin.algo.datamodel.TrialMatch;
import com.hartwig.actin.treatment.datamodel.CohortMetadata;
import com.hartwig.actin.treatment.datamodel.TrialIdentification;

import org.jetbrains.annotations.NotNull;

public final class TrialMatchSummarizer {

    private TrialMatchSummarizer() {
    }

    @NotNull
    public static TrialMatchSummary summarize(@NotNull List<TrialMatch> trialMatches) {
        int trialCount = 0;
        int cohortCount = 0;

        Map<TrialIdentification, List<CohortMetadata>> eligibleTrialMap = Maps.newHashMap();
        for (TrialMatch trial : trialMatches) {
            trialCount++;
            // A trial without cohorts is considered a cohort on its own.
            boolean hasCohorts = !trial.cohorts().isEmpty();
            cohortCount += hasCohorts ? trial.cohorts().size() : 1;

            if (trial.isPotentiallyEligible()) {
                List<CohortMetadata> eligibleCohorts = Lists.newArrayList();
                for (CohortMatch cohort : trial.cohorts()) {
                    if (cohort.isPotentiallyEligible()) {
                        eligibleCohorts.add(cohort.metadata());
                    }
                }

                eligibleTrialMap.put(trial.identification(), eligibleCohorts);
            }
        }

        return ImmutableTrialMatchSummary.builder()
                .trialCount(trialCount)
                .cohortCount(cohortCount)
                .eligibleTrialMap(eligibleTrialMap)
                .build();
    }
}
