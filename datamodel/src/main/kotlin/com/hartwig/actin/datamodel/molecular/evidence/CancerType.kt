package com.hartwig.actin.datamodel.molecular.evidence

data class CancerType(
    val matchedCancerType: String,
    val excludedCancerSubTypes: Set<String>
)
