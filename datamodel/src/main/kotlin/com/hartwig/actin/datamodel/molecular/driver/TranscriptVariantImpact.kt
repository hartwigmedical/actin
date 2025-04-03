package com.hartwig.actin.datamodel.molecular.driver

data class TranscriptVariantImpact(
    val transcriptId: String,
    val hgvsCodingImpact: String,
    val hgvsProteinImpact: String,
    val hgvsProteinImpact3: String,
    val affectedCodon: Int? = null,
    val affectedExon: Int? = null,
    val isSpliceRegion: Boolean?,
    val effects: Set<VariantEffect> = emptySet(),
    val codingEffect: CodingEffect? = null
) : Comparable<TranscriptVariantImpact> {

    override fun compareTo(other: TranscriptVariantImpact): Int {
        return transcriptId.compareTo(other.transcriptId)
    }
}
