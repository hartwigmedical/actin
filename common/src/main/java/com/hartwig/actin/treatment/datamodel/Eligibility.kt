package com.hartwig.actin.treatment.datamodel;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Eligibility {

    @NotNull
    public abstract Set<CriterionReference> references();

    @NotNull
    public abstract EligibilityFunction function();

}
