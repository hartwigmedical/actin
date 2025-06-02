package com.hartwig.actin.datamodel.molecular.driver

object TestTranscriptCopyNumberImpactFactory {

    fun createTranscriptCopyNumberImpact(
        type: CopyNumberType = CopyNumberType.NONE,
        minCopies: Int? = null,
        maxCopies: Int? = null
    ): TranscriptCopyNumberImpact {
        return TranscriptCopyNumberImpact(
            transcriptId = "",
            type = type,
            minCopies = minCopies,
            maxCopies = maxCopies
        )
    }
}