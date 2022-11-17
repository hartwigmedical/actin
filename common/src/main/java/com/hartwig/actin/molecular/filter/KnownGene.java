package com.hartwig.actin.molecular.filter;

import com.hartwig.actin.molecular.datamodel.driver.GeneRole;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class KnownGene {

    @NotNull
    public abstract String gene();

    @NotNull
    public abstract GeneRole geneRole();
}
