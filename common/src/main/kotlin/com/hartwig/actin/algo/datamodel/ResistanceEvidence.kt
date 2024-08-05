package com.hartwig.actin.algo.datamodel

data class ResistanceEvidence(
    val event: String,
    val isTested: Boolean?,
    val isFound: Boolean?,
    val resistanceLevel: String,
    val evidenceUrls: Set<String>,
    val treatmentName: String
)