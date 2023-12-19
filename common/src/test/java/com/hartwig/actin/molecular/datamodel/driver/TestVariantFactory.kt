package com.hartwig.actin.molecular.datamodel.driver

import org.apache.logging.log4j.util.Strings

object TestVariantFactory {
    @JvmStatic
    fun builder(): ImmutableVariant.Builder {
        return ImmutableVariant.builder()
            .from(TestDriverFactory.createEmptyDriver())
            .gene(Strings.EMPTY)
            .geneRole(GeneRole.UNKNOWN)
            .proteinEffect(ProteinEffect.UNKNOWN)
            .type(VariantType.SNV)
            .variantCopyNumber(0.0)
            .totalCopyNumber(0.0)
            .isBiallelic(false)
            .isHotspot(false)
            .clonalLikelihood(0.0)
            .canonicalImpact(TestTranscriptImpactFactory.builder().build())
    }
}
