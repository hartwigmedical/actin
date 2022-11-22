package com.hartwig.actin.molecular.orange.datamodel.purple;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class PurpleFit {

    public abstract boolean hasReliableQuality();

    public abstract boolean hasReliablePurity();

    public abstract double purity();

    public abstract double ploidy();
}
