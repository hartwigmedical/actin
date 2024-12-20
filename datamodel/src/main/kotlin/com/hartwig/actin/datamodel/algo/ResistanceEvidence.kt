package com.hartwig.actin.datamodel.algo

data class ResistanceEvidence(
    val event: String,
    val treatmentName: String,
    val resistanceLevel: String,
    val isTested: Boolean?,
    val isFound: Boolean?,
    val evidenceUrls: Set<String>
)