package com.hartwig.actin.molecular.datamodel.immunology;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class HlaAllele {

    @NotNull
    public abstract String name();

    public abstract double tumorCopyNumber();

    public abstract boolean hasSomaticMutations();
}
