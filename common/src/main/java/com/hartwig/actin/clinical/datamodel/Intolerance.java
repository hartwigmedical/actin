package com.hartwig.actin.clinical.datamodel;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Intolerance {

    @NotNull
    public abstract String name();

    @NotNull
    public abstract Set<String> doids();

    @NotNull
    public abstract String category();

    @NotNull
    public abstract Set<String> subcategories();

    @NotNull
    public abstract String type();

    @NotNull
    public abstract String clinicalStatus();

    @NotNull
    public abstract String verificationStatus();

    @NotNull
    public abstract String criticality();

}
