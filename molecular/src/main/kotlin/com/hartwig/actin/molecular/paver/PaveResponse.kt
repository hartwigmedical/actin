package com.hartwig.actin.molecular.paver

data class PaveResponse(
    val id: String,
    val impact: PaveImpact,
    val transcriptImpact: List<PaveTranscriptImpact>,
)

