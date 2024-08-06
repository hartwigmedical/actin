package com.hartwig.actin.molecular.paver

data class PaveTranscriptImpact(
    val geneId: String,
    val gene: String,
    val transcript: String,
    val effects: List<PaveVariantEffect>,
    val spliceRegion: Boolean,
    val hgvsCodingImpact: String,
    val hgvsProteinImpact: String,
)