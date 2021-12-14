package com.hartwig.actin.serve.datamodel;

import java.util.List;

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

    @NotNull
    public abstract List<String> parameters();
}
