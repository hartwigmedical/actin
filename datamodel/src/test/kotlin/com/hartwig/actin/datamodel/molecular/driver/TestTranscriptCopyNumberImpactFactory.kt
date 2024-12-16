package com.hartwig.actin.datamodel.molecular.driver

import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.orange.driver.TranscriptCopyNumberImpact

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