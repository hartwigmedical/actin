package com.hartwig.actin.algo.sort

import com.hartwig.actin.datamodel.algo.CohortMatch
import com.hartwig.actin.trial.sort.CohortMetadataComparator

class CohortMatchComparator : Comparator<CohortMatch> {

    override fun compare(eligibility1: CohortMatch, eligibility2: CohortMatch): Int {
        return Comparator.comparing(CohortMatch::metadata, CohortMetadataComparator())
            .thenComparing(CohortMatch::isPotentiallyEligible)
            .thenComparing(CohortMatch::evaluations, EvaluationMapComparator())
            .compare(eligibility1, eligibility2)
    }
}
