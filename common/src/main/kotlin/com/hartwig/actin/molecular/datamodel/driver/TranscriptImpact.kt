package com.hartwig.actin.molecular.datamodel.driver

data class TranscriptImpact(
    val transcriptId: String,
    val hgvsCodingImpact: String,
    val hgvsProteinImpact: String,
    val affectedCodon: Int?,
    val affectedExon: Int?,
    val isSpliceRegion: Boolean,
    val effects: Set<VariantEffect>,
    val codingEffect: CodingEffect?
)
