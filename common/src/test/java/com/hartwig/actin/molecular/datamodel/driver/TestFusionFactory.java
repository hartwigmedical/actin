package com.hartwig.actin.molecular.datamodel.driver;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestFusionFactory {

    private TestFusionFactory() {
    }

    @NotNull
    public static ImmutableFusion.Builder builder() {
        return ImmutableFusion.builder()
                .from(TestDriverFactory.createEmptyDriver())
                .fiveGene(Strings.EMPTY)
                .threeGene(Strings.EMPTY)
                .proteinEffect(ProteinEffect.UNKNOWN)
                .details(Strings.EMPTY)
                .driverType(FusionDriverType.KNOWN);
    }
}
