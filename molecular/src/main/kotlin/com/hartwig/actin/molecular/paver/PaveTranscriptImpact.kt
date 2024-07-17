package com.hartwig.actin.molecular.paver

data class PaveTranscriptImpact(
    val gene: String,
    val geneName: String,
    val transcript: String,
    val effects: List<PaveVariantEffect>,
    val spliceRegion: Boolean,
    val hgvsCodingImpact: String,
    val hgvsProteinImpact: String,
)