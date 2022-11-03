package com.hartwig.actin.molecular.datamodel.driver;

import com.hartwig.actin.molecular.datamodel.evidence.ImmutableActionableEvidence;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestVariantFactory {

    private TestVariantFactory() {
    }

    @NotNull
    public static ImmutableVariant.Builder builder() {
        return ImmutableVariant.builder()
                .driverLikelihood(DriverLikelihood.LOW)
                .evidence(ImmutableActionableEvidence.builder().build())
                .gene(Strings.EMPTY)
                .geneRole(GeneRole.UNKNOWN)
                .proteinEffect(ProteinEffect.UNKNOWN)
                .type(VariantType.SNV)
                .variantCopyNumber(0D)
                .totalCopyNumber(0D)
                .isBiallelic(false)
                .isHotspot(false)
                .clonalLikelihood(0D);
    }
}
