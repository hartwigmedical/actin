package com.hartwig.actin.algo.sort

import com.hartwig.actin.algo.datamodel.CohortMatch
import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.treatment.sort.TrialIdentificationComparator
import kotlin.Int

class TrialMatchComparator : Comparator<TrialMatch> {

    override fun compare(match1: TrialMatch, match2: TrialMatch): Int {
        return Comparator.comparing(TrialMatch::identification, TrialIdentificationComparator())
            .thenComparing(TrialMatch::isPotentiallyEligible)
            .thenComparing({ it.cohorts.size }, Int::compareTo)
            .thenComparing({ it.cohorts }, ::compareCohortMatches)
            .thenComparing(TrialMatch::evaluations, EvaluationMapComparator())
            .compare(match1, match2)
    }

    private fun compareCohortMatches(match1: List<CohortMatch>, match2: List<CohortMatch>): Int {
        return match1.indices.map { index ->
            COHORT_ELIGIBILITY_COMPARATOR.compare(match1[index], match2[index])
        }
            .find { it != 0 } ?: 0
    }

    companion object {
        private val COHORT_ELIGIBILITY_COMPARATOR = CohortMatchComparator()
    }
}
