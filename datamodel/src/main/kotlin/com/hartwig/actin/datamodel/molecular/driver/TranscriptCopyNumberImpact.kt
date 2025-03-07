package com.hartwig.actin.datamodel.molecular.driver

data class TranscriptCopyNumberImpact(
    val transcriptId: String,
    val type: CopyNumberType,
    val minCopies: Int,
    val maxCopies: Int,
) : Comparable<TranscriptCopyNumberImpact> {

    override fun compareTo(other: TranscriptCopyNumberImpact): Int {
        return transcriptId.compareTo(other.transcriptId)
    }
}