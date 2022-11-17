package com.hartwig.actin.molecular.serve;

import com.hartwig.actin.molecular.datamodel.driver.GeneRole;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestKnownGeneFactory {

    private TestKnownGeneFactory() {
    }

    @NotNull
    public static ImmutableKnownGene.Builder builder() {
        return ImmutableKnownGene.builder().gene(Strings.EMPTY).geneRole(GeneRole.UNKNOWN);
    }
}
