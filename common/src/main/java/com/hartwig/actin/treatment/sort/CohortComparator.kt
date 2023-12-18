package com.hartwig.actin.treatment.sort

import com.hartwig.actin.treatment.datamodel.Cohort
import com.hartwig.actin.treatment.datamodel.CohortMetadata
import com.hartwig.actin.treatment.datamodel.Eligibility

class CohortComparator() : Comparator<Cohort> {
    public override fun compare(cohort1: Cohort, cohort2: Cohort): Int {
        val metadataCompare: Int = METADATA_COMPARATOR.compare(cohort1.metadata(), cohort2.metadata())
        if (metadataCompare != 0) {
            return metadataCompare
        }
        val sizeCompare: Int = cohort1.eligibility().size - cohort2.eligibility().size
        if (sizeCompare != 0) {
            return if (sizeCompare > 0) 1 else -1
        }
        var index: Int = 0
        while (index < cohort1.eligibility().size) {
            val eligibilityCompare: Int = ELIGIBILITY_COMPARATOR.compare(cohort1.eligibility().get(index), cohort2.eligibility().get(index))
            if (eligibilityCompare != 0) {
                return eligibilityCompare
            }
            index++
        }
        return 0
    }

    companion object {
        private val METADATA_COMPARATOR: Comparator<CohortMetadata> = CohortMetadataComparator()
        private val ELIGIBILITY_COMPARATOR: Comparator<Eligibility> = EligibilityComparator()
    }
}
