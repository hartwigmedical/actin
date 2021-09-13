package com.hartwig.actin.clinical.datamodel;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class HistoryTumorTreatment {

    @NotNull
    public abstract String name();

    public abstract int year();

    public abstract boolean isSystemic();

    public abstract boolean isChemotherapy();

    public abstract boolean isImmunotherapy();

    @Nullable
    public abstract String immunotherapyType();

    public abstract boolean isTargetedTherapy();

    public abstract boolean isHormoneTherapy();

    public abstract boolean isStemCellTransplant();

    public abstract boolean isRadiotherapy();

    public abstract boolean isSurgery();

}
