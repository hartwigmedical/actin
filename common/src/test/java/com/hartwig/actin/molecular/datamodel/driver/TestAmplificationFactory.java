package com.hartwig.actin.molecular.datamodel.driver;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestAmplificationFactory {

    private TestAmplificationFactory() {
    }

    @NotNull
    public static ImmutableAmplification.Builder builder() {
        return ImmutableAmplification.builder()
                .from(TestDriverFactory.createEmptyDriver())
                .gene(Strings.EMPTY)
                .geneRole(GeneRole.UNKNOWN)
                .proteinEffect(ProteinEffect.UNKNOWN)
                .minCopies(0)
                .maxCopies(0);
    }
}
