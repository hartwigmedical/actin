package com.hartwig.actin.algo.interpretation

import com.hartwig.actin.datamodel.trial.CohortMetadata
import com.hartwig.actin.datamodel.trial.TrialIdentification

data class TrialMatchSummary(
    val trialCount: Int = 0,
    val cohortCount: Int = 0,
    val eligibleTrialMap: Map<TrialIdentification, List<CohortMetadata>> = emptyMap()
) {

    operator fun plus(other: TrialMatchSummary) = TrialMatchSummary(
        trialCount = trialCount + other.trialCount,
        cohortCount = cohortCount + other.cohortCount,
        eligibleTrialMap = eligibleTrialMap + other.eligibleTrialMap
    )
}
