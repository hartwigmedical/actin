package com.hartwig.actin.algo.interpretation

import com.hartwig.actin.algo.datamodel.CohortMatch
import com.hartwig.actin.algo.datamodel.TrialMatch

object TrialMatchSummarizer {

    fun summarize(trialMatches: List<TrialMatch>): TrialMatchSummary {
        return trialMatches.map { trial ->
            val eligibleTrialMap = if (!trial.isPotentiallyEligible) emptyMap() else {
                mapOf(trial.identification to trial.cohorts.filter(CohortMatch::isPotentiallyEligible).map(CohortMatch::metadata))
            }
            TrialMatchSummary(1, trial.cohorts.size, eligibleTrialMap)
        }.fold(TrialMatchSummary(), TrialMatchSummary::plus)
    }
}
