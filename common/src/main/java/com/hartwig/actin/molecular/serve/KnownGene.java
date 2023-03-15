package com.hartwig.actin.molecular.serve;

import com.hartwig.actin.molecular.datamodel.driver.GeneRole;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO (ACTIN-4) Move into serve
@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class KnownGene {

    @NotNull
    public abstract String gene();

    @NotNull
    public abstract GeneRole geneRole();
}
