package com.hartwig.actin.datamodel.molecular.evidence

data class EvidenceDirection(
    val hasPositiveResponse: Boolean,
    val hasBenefit: Boolean,
    val isResistant: Boolean,
    val isCertain: Boolean
)
