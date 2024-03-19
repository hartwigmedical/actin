package com.hartwig.actin.algo.sort

import com.hartwig.actin.algo.datamodel.CohortMatch
import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.trial.sort.TrialIdentificationComparator
import kotlin.Int

class TrialMatchComparator : Comparator<TrialMatch> {

    private val cohortEligibilityComparator = CohortMatchComparator()

    private val comparator = Comparator.comparing(TrialMatch::identification, TrialIdentificationComparator())
        .thenComparing(TrialMatch::isPotentiallyEligible)
        .thenComparing({ it.cohorts.size }, Int::compareTo)
        .thenComparing({ it.cohorts }, ::compareCohortMatches)
        .thenComparing(TrialMatch::evaluations, EvaluationMapComparator())

    override fun compare(match1: TrialMatch, match2: TrialMatch): Int {
        return comparator.compare(match1, match2)
    }

    private fun compareCohortMatches(matches1: List<CohortMatch>, matches2: List<CohortMatch>): Int {
        return matches1.zip(matches2).map { (match1, match2) ->
            cohortEligibilityComparator.compare(match1, match2)
        }
            .find { it != 0 } ?: 0
    }
}
