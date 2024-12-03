package com.hartwig.actin.datamodel.molecular.evidence

data class EvidenceDirection(
    val hasPositiveResponse: Boolean = false,
    val hasBenefit: Boolean = false,
    val isResistant: Boolean = false,
    val isCertain: Boolean = false
)
