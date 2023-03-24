package com.hartwig.actin.algo.soc.datamodel;

import java.util.Set;

import com.hartwig.actin.clinical.datamodel.TreatmentCategory;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Treatment {

    @NotNull
    public abstract String name();

    public abstract boolean isOptional();

    public abstract int score();

    @NotNull
    public abstract Set<TreatmentComponent> components();

    @NotNull
    public abstract Set<TreatmentCategory> categories();

    @NotNull
    public abstract Set<EligibilityFunction> eligibilityFunctions();

    @NotNull
    public abstract Set<Integer> lines();
}
