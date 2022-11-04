package com.hartwig.actin.molecular.datamodel.driver;

import com.hartwig.actin.molecular.datamodel.evidence.ImmutableActionableEvidence;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestDisruptionFactory {

    private TestDisruptionFactory() {
    }

    @NotNull
    public static ImmutableDisruption.Builder builder() {
        return ImmutableDisruption.builder()
                .driverLikelihood(DriverLikelihood.LOW)
                .evidence(ImmutableActionableEvidence.builder().build())
                .gene(Strings.EMPTY)
                .geneRole(GeneRole.UNKNOWN)
                .proteinEffect(ProteinEffect.UNKNOWN)
                .type(Strings.EMPTY)
                .junctionCopyNumber(0)
                .undisruptedCopyNumber(0)
                .regionType(RegionType.INTRONIC)
                .codingContext(CodingContext.NON_CODING)
                .range(Strings.EMPTY);
    }
}
