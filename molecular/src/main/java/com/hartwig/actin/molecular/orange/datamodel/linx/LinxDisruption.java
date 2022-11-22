package com.hartwig.actin.molecular.orange.datamodel.linx;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class LinxDisruption {

    public abstract boolean reported();

    @NotNull
    public abstract String gene();

    @NotNull
    public abstract String type();

    public abstract double junctionCopyNumber();

    public abstract double undisruptedCopyNumber();

    @NotNull
    public abstract LinxRegionType regionType();

    @NotNull
    public abstract LinxCodingType codingType();

    @Nullable
    public abstract Integer clusterId();
}
