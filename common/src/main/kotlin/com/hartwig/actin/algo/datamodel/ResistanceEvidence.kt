package com.hartwig.actin.algo.datamodel

data class ResistanceEvidence(
    val event: String,
    val isTested: Boolean?,
    val isFound: Boolean?,
    val resistanceLevel: String, // = logica known vs suspect
    val evidenceUrls: Set<String>
)

// serve, doid, molecular results.
// on-label