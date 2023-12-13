package com.hartwig.actin.algo.interpretation

import com.google.common.collect.Lists
import com.google.common.collect.Maps
import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.treatment.datamodel.CohortMetadata
import com.hartwig.actin.treatment.datamodel.TrialIdentification

object TrialMatchSummarizer {
    fun summarize(trialMatches: List<TrialMatch>): TrialMatchSummary {
        var trialCount = 0
        var cohortCount = 0
        val eligibleTrialMap: MutableMap<TrialIdentification, List<CohortMetadata>> = Maps.newHashMap()
        for (trial in trialMatches) {
            trialCount++
            // A trial without cohorts is considered a cohort on its own.
            val hasCohorts = !trial.cohorts().isEmpty()
            cohortCount += if (hasCohorts) trial.cohorts().size else 1
            if (trial.isPotentiallyEligible()) {
                val eligibleCohorts: MutableList<CohortMetadata> = Lists.newArrayList()
                for (cohort in trial.cohorts()) {
                    if (cohort!!.isPotentiallyEligible()) {
                        eligibleCohorts.add(cohort.metadata())
                    }
                }
                eligibleTrialMap[trial.identification()] = eligibleCohorts
            }
        }
        return ImmutableTrialMatchSummary.builder()
            .trialCount(trialCount)
            .cohortCount(cohortCount)
            .eligibleTrialMap(eligibleTrialMap)
            .build()
    }
}
