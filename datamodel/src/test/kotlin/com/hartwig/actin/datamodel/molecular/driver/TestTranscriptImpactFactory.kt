package com.hartwig.actin.datamodel.molecular.driver

import com.hartwig.actin.datamodel.molecular.TranscriptImpact

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
