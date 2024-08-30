package com.hartwig.actin.datamodel.algo

data class ResistanceEvidence(
    val event: String,
    val isTested: Boolean?,
    val isFound: Boolean?,
    val resistanceLevel: String,
    val evidenceUrls: Set<String>,
    val treatmentName: String
)