package com.hartwig.actin.clinical.datamodel;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class CypInteraction {

    public enum Type {
        INDUCER,
        INHIBITOR,
        SUBSTRATE
    }

    public enum Strength {
        STRONG,
        MODERATE,
        WEAK,
        SENSITIVE,
        MODERATE_SENSITIVE
    }

    @NotNull
    public abstract Type type();

    @NotNull
    public abstract Strength strength();

    @NotNull
    public abstract String cyp();
}
