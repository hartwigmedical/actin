package com.hartwig.actin.molecular.datamodel.driver

import com.hartwig.actin.molecular.datamodel.TranscriptImpact

object TestTranscriptImpactFactory {

    fun createMinimal(): TranscriptImpact {
        return TranscriptImpact(
            transcriptId = "",
            hgvsCodingImpact = "",
            hgvsProteinImpact = "",
            affectedCodon = null,
            affectedExon = null,
            isSpliceRegion = false,
            effects = emptySet(),
            codingEffect = null
        )
    }
}
