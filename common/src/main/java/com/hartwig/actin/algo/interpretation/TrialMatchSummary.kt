package com.hartwig.actin.algo.interpretation

import com.hartwig.actin.trial.datamodel.CohortMetadata
import com.hartwig.actin.trial.datamodel.TrialIdentification

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
