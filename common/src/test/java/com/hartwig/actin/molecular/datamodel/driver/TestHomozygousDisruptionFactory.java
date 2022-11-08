package com.hartwig.actin.molecular.datamodel.driver;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestHomozygousDisruptionFactory {

    private TestHomozygousDisruptionFactory() {
    }

    @NotNull
    public static ImmutableHomozygousDisruption.Builder builder() {
        return ImmutableHomozygousDisruption.builder()
                .from(TestDriverFactory.createEmptyDriver())
                .gene(Strings.EMPTY)
                .geneRole(GeneRole.UNKNOWN)
                .proteinEffect(ProteinEffect.UNKNOWN);
    }
}
