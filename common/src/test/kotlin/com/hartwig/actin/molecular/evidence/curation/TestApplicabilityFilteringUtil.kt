package com.hartwig.actin.molecular.evidence.curation

object TestApplicabilityFilteringUtil {

    fun nonApplicableGene(): String {
        return ApplicabilityFiltering.NON_APPLICABLE_GENES.iterator().next()
    }

    fun nonApplicableAmplification(): String {
        return ApplicabilityFiltering.NON_APPLICABLE_AMPLIFICATIONS.iterator().next()
    }
}
