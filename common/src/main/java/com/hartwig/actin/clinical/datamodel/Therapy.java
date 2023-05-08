package com.hartwig.actin.clinical.datamodel;

import java.util.List;
import java.util.Set;

import com.hartwig.actin.treatment.datamodel.EligibilityFunction;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Therapy implements Treatment {

    @NotNull
    public abstract String name();

    @Nullable
    public abstract Therapy therapy();

    public abstract boolean isSystemic();

    public abstract boolean isOptional();

    public abstract int score();

    @NotNull
    public abstract Set<TreatmentTypes> types();

    @NotNull
    public abstract Set<TreatmentCategory> categories();

    @NotNull
    public abstract Set<EligibilityFunction> eligibilityFunctions();

    @NotNull
    public abstract Set<Integer> lines();

    @NotNull
    public abstract Set<String> synonyms();

    @NotNull
    public abstract List<Drug> drugs();

    public abstract int maxCycles();
}
