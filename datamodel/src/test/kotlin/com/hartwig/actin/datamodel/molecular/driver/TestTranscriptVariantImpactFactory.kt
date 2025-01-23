package com.hartwig.actin.datamodel.molecular.driver

object TestTranscriptVariantImpactFactory {

    fun createMinimal(): TranscriptVariantImpact {
        return TranscriptVariantImpact(
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
