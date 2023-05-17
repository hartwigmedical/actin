package com.hartwig.actin.clinical.datamodel;

import java.util.Set;

import com.hartwig.actin.treatment.datamodel.EligibilityFunction;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class RecommendationCriteria {

    public abstract boolean isOptional();

    public abstract int score();

    @NotNull
    public abstract Set<EligibilityFunction> eligibilityFunctions();

    @NotNull
    public abstract Set<Integer> lines();
}
