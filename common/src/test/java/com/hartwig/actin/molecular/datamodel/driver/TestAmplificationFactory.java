package com.hartwig.actin.molecular.datamodel.driver;

import com.hartwig.actin.molecular.datamodel.evidence.ImmutableActionableEvidence;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestAmplificationFactory {

    private TestAmplificationFactory() {
    }

    @NotNull
    public static ImmutableAmplification.Builder builder() {
        return ImmutableAmplification.builder()
                .driverLikelihood(DriverLikelihood.LOW)
                .evidence(ImmutableActionableEvidence.builder().build())
                .gene(Strings.EMPTY)
                .geneRole(GeneRole.UNKNOWN)
                .proteinEffect(ProteinEffect.UNKNOWN)
                .isPartial(false)
                .copies(0);
    }
}
