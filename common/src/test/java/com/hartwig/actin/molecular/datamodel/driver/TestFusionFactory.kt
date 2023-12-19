package com.hartwig.actin.molecular.datamodel.driver

import org.apache.logging.log4j.util.Strings

object TestFusionFactory {
    @JvmStatic
    fun builder(): ImmutableFusion.Builder {
        return ImmutableFusion.builder()
            .from(TestDriverFactory.createEmptyDriver())
            .geneStart(Strings.EMPTY)
            .geneTranscriptStart(Strings.EMPTY)
            .fusedExonUp(-1)
            .geneEnd(Strings.EMPTY)
            .geneTranscriptEnd(Strings.EMPTY)
            .fusedExonDown(-1)
            .proteinEffect(ProteinEffect.UNKNOWN)
            .driverType(FusionDriverType.KNOWN_PAIR)
    }
}
