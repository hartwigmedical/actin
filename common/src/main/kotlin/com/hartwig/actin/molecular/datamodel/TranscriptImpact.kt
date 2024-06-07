package com.hartwig.actin.molecular.datamodel

data class TranscriptImpact(
    val transcriptId: String,
    val hgvsCodingImpact: String,
    val hgvsProteinImpact: String,
    val affectedCodon: Int? = null,
    val affectedExon: Int? = null,
    val isSpliceRegion: Boolean,
    val effects: Set<VariantEffect> = emptySet(),
    val codingEffect: CodingEffect? = null
) : Comparable<TranscriptImpact> {

    override fun compareTo(other: TranscriptImpact): Int {
        return transcriptId.compareTo(other.transcriptId)
    }
}
