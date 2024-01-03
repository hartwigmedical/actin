package com.hartwig.actin.molecular.datamodel.driver

object TestTranscriptImpactFactory {

    fun createMinimal(): TranscriptImpact {
        return TranscriptImpact(
            transcriptId = "",
            hgvsCodingImpact = "",
            hgvsProteinImpact = "",
            isSpliceRegion = false,
            affectedCodon = null,
            affectedExon = null,
            codingEffect = null,
            effects = emptySet()
        )
    }
}
