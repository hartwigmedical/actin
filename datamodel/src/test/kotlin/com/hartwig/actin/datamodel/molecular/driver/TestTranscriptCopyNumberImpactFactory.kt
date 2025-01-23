package com.hartwig.actin.datamodel.molecular.driver

object TestTranscriptCopyNumberImpactFactory {

    fun createTranscriptCopyNumberImpact(
        type: CopyNumberType = CopyNumberType.NONE,
        minCopies: Int = 0,
        maxCopies: Int = 0
    ): TranscriptCopyNumberImpact {
        return TranscriptCopyNumberImpact(
            transcriptId = "",
            type = type,
            minCopies = minCopies,
            maxCopies = maxCopies
        )
    }
}