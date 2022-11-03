package com.hartwig.actin.molecular.datamodel.driver;

import com.hartwig.actin.molecular.datamodel.evidence.ImmutableActionableEvidence;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestLossFactory {

    private TestLossFactory() {
    }

    @NotNull
    public static ImmutableLoss.Builder builder() {
        return ImmutableLoss.builder()
                .driverLikelihood(DriverLikelihood.LOW)
                .evidence(ImmutableActionableEvidence.builder().build())
                .gene(Strings.EMPTY)
                .geneRole(GeneRole.UNKNOWN)
                .proteinEffect(ProteinEffect.UNKNOWN)
                .isPartial(false);
    }
}
