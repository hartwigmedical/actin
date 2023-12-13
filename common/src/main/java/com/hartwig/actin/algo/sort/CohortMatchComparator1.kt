package com.hartwig.actin.algo.sort

import com.hartwig.actin.algo.datamodel.CohortMatch
import com.hartwig.actin.treatment.datamodel.CohortMetadata
import com.hartwig.actin.treatment.sort.CohortMetadataComparator
import java.lang.Boolean
import kotlin.Comparator
import kotlin.Int

class CohortMatchComparator : Comparator<CohortMatch> {
    override fun compare(eligibility1: CohortMatch, eligibility2: CohortMatch): Int {
        val metadataCompare = METADATA_COMPARATOR.compare(eligibility1.metadata(), eligibility2.metadata())
        if (metadataCompare != 0) {
            return metadataCompare
        }
        val isPotentiallyEligibleCompare = Boolean.compare(eligibility1.isPotentiallyEligible(), eligibility2.isPotentiallyEligible())
        return if (isPotentiallyEligibleCompare != 0) {
            isPotentiallyEligibleCompare
        } else EvaluationMapCompare.compare(eligibility1.evaluations(), eligibility2.evaluations())
    }

    companion object {
        private val METADATA_COMPARATOR: Comparator<CohortMetadata> = CohortMetadataComparator()
    }
}
