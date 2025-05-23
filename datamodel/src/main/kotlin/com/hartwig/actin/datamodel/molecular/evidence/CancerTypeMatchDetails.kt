package com.hartwig.actin.datamodel.molecular.evidence

enum class CancerTypeMatchApplicability {
    SPECIFIC_TYPE,
    ALL_TYPES,
    OTHER_TYPE;

    fun isOnLabel() = this == SPECIFIC_TYPE || this == ALL_TYPES
}

data class CancerTypeMatchDetails(val cancerType: CancerType, val applicability: CancerTypeMatchApplicability)