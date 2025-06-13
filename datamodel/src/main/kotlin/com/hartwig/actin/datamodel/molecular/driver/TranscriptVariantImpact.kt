package com.hartwig.actin.datamodel.molecular.driver

data class TranscriptVariantImpact(
    val transcriptId: String,
    val hgvsCodingImpact: String,
    val hgvsProteinImpact: String,
    val affectedCodon: Int?,
    val affectedExon: Int?,
    val inSpliceRegion: Boolean?,
    val effects: Set<VariantEffect>,
    val codingEffect: CodingEffect?
) : Comparable<TranscriptVariantImpact> {

    override fun compareTo(other: TranscriptVariantImpact): Int {
        return transcriptId.compareTo(other.transcriptId)
    }
}
