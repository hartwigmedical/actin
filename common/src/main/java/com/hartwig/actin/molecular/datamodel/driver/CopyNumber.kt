package com.hartwig.actin.molecular.datamodel.driver;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class CopyNumber implements Driver, GeneAlteration {

    @NotNull
    public abstract CopyNumberType type();

    public abstract int minCopies();

    public abstract int maxCopies();
}
