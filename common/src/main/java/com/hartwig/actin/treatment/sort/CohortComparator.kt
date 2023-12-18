package com.hartwig.actin.treatment.sort

import com.hartwig.actin.treatment.datamodel.Cohort
import com.hartwig.actin.treatment.datamodel.CohortMetadata
import com.hartwig.actin.treatment.datamodel.Eligibility

class CohortComparator : Comparator<Cohort> {
    private val comparator = Comparator.comparing(Cohort::metadata, METADATA_COMPARATOR)
        .thenComparing({ it.eligibility.size }, Int::compareTo)
        .thenComparing(Cohort::eligibility, ::compareCohortEligibilities)

    override fun compare(cohort1: Cohort, cohort2: Cohort): Int {
        return comparator.compare(cohort1, cohort2)
    }

    private fun compareCohortEligibilities(eligibilities1: List<Eligibility>, eligibilities2: List<Eligibility>): Int {
        return eligibilities1.zip(eligibilities2).map { (eligibility1, eligibility2) ->
            ELIGIBILITY_COMPARATOR.compare(eligibility1, eligibility2)
        }
            .find { it != 0 } ?: 0
    }

    companion object {
        private val METADATA_COMPARATOR: Comparator<CohortMetadata> = CohortMetadataComparator()
        private val ELIGIBILITY_COMPARATOR: Comparator<Eligibility> = EligibilityComparator()
    }
}
