package com.hartwig.actin.molecular.datamodel.driver;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestCopyNumberFactory {

    private TestCopyNumberFactory() {
    }

    @NotNull
    public static ImmutableCopyNumber.Builder builder() {
        return ImmutableCopyNumber.builder()
                .from(TestDriverFactory.createEmptyDriver())
                .gene(Strings.EMPTY)
                .geneRole(GeneRole.UNKNOWN)
                .proteinEffect(ProteinEffect.UNKNOWN)
                .type(CopyNumberType.NONE)
                .minCopies(0)
                .maxCopies(0);
    }
}
