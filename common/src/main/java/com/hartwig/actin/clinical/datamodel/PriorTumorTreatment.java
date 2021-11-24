package com.hartwig.actin.clinical.datamodel;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class PriorTumorTreatment {

    @NotNull
    public abstract String name();

    @Nullable
    public abstract Integer year();

    @Nullable
    public abstract Integer month();

    @NotNull
    public abstract Set<TreatmentCategory> categories();

    public abstract boolean isSystemic();

    @Nullable
    public abstract String chemoType();

    @Nullable
    public abstract String immunoType();

    @Nullable
    public abstract String targetedType();

    @Nullable
    public abstract String hormoneType();

    @Nullable
    public abstract String stemCellTransType();

    @Nullable
    public abstract String supportiveType();

}
