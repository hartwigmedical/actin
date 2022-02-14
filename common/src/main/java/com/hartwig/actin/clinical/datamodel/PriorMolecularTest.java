package com.hartwig.actin.clinical.datamodel;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class PriorMolecularTest {

    @NotNull
    public abstract String test();

    @NotNull
    public abstract String item();

    @Nullable
    public abstract String measure();

    public abstract double score();

    @Nullable
    public abstract String unit();
}
