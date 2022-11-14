package com.hartwig.actin.molecular.datamodel.driver;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestLossFactory {

    private TestLossFactory() {
    }

    @NotNull
    public static ImmutableLoss.Builder builder() {
        return ImmutableLoss.builder()
                .from(TestDriverFactory.createEmptyDriver())
                .gene(Strings.EMPTY)
                .geneRole(GeneRole.UNKNOWN)
                .proteinEffect(ProteinEffect.UNKNOWN)
                .minCopies(0)
                .maxCopies(0)
                .isPartial(false);
    }
}
