package com.hartwig.actin.datamodel.molecular.driver

data class TranscriptVariantImpact(
    val transcriptId: String,
    val hgvsCodingImpact: String,
    val hgvsProteinImpact: String,
    val affectedCodon: Int? = null,
    val affectedExon: Int? = null,
    val inSpliceRegion: Boolean?,
    val effects: Set<VariantEffect>,
    val codingEffect: CodingEffect?
) : Comparable<TranscriptVariantImpact> {

    override fun compareTo(other: TranscriptVariantImpact): Int {
        return transcriptId.compareTo(other.transcriptId)
    }
}
