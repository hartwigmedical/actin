package com.hartwig.actin.molecular.datamodel.driver

import org.apache.logging.log4j.util.Strings

object TestTranscriptImpactFactory {
    @JvmStatic
    fun builder(): ImmutableTranscriptImpact.Builder {
        return ImmutableTranscriptImpact.builder()
            .transcriptId(Strings.EMPTY)
            .hgvsCodingImpact(Strings.EMPTY)
            .hgvsProteinImpact(Strings.EMPTY)
            .isSpliceRegion(false)
    }
}
