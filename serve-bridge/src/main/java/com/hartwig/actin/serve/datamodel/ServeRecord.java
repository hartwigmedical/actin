package com.hartwig.actin.serve.datamodel;

import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class ServeRecord {

    @NotNull
    public abstract String trial();

    @NotNull
    public abstract EligibilityRule rule();

    @Nullable
    public abstract String gene();

    @Nullable
    public abstract String mutation();

    public abstract boolean isUsedAsInclusion();
}
