package com.hartwig.actin.molecular.datamodel.driver;

import com.hartwig.actin.molecular.datamodel.evidence.ImmutableActionableEvidence;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestFusionFactory {

    private TestFusionFactory() {
    }

    @NotNull
    public static ImmutableFusion.Builder builder() {
        return ImmutableFusion.builder()
                .driverLikelihood(DriverLikelihood.LOW)
                .evidence(ImmutableActionableEvidence.builder().build())
                .fiveGene(Strings.EMPTY)
                .threeGene(Strings.EMPTY)
                .proteinEffect(ProteinEffect.UNKNOWN)
                .details(Strings.EMPTY)
                .driverType(FusionDriverType.KNOWN);
    }
}
