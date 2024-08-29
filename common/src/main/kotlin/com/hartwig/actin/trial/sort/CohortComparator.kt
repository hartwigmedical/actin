package com.hartwig.actin.trial.sort

import com.hartwig.actin.datamodel.trial.Cohort
import com.hartwig.actin.datamodel.trial.CohortMetadata
import com.hartwig.actin.datamodel.trial.Eligibility

class CohortComparator : Comparator<Cohort> {

    private val metadataComparator: Comparator<CohortMetadata> = CohortMetadataComparator()
    private val eligibilityComparator: Comparator<Eligibility> = EligibilityComparator()

    private val comparator = Comparator.comparing(Cohort::metadata, metadataComparator)
        .thenComparing({ it.eligibility.size }, Int::compareTo)
        .thenComparing(Cohort::eligibility, ::compareCohortEligibilities)

    override fun compare(cohort1: Cohort, cohort2: Cohort): Int {
        return comparator.compare(cohort1, cohort2)
    }

    private fun compareCohortEligibilities(eligibilities1: List<Eligibility>, eligibilities2: List<Eligibility>): Int {
        return eligibilities1.zip(eligibilities2).map { (eligibility1, eligibility2) ->
            eligibilityComparator.compare(eligibility1, eligibility2)
        }
            .find { it != 0 } ?: 0
    }
}
