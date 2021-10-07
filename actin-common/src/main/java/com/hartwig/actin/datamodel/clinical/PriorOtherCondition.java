package com.hartwig.actin.datamodel.clinical;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class PriorOtherCondition {

    @NotNull
    public abstract String name();

    @NotNull
    public abstract Set<String> doids();

    @NotNull
    public abstract String category();

}
