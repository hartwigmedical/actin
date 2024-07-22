package com.hartwig.actin.algo.datamodel

data class ResistanceEvidence(
    val event: String, // = sourceEvent
    val isTested: Boolean, // voor nu null
    val isFound: Boolean, // voor nu null
    val resistanceLevel: String, // = logica known vs suspect
    val evidenceUrls: Set<String>
)

// serve, doid, molecular results.
// on-label