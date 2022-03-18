package com.hartwig.actin.algo.datamodel;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Evaluation {

    @NotNull
    public abstract EvaluationResult result();

    public abstract boolean recoverable();

    @NotNull
    public abstract Set<String> passSpecificMessages();

    @NotNull
    public abstract Set<String> passGeneralMessages();

    @NotNull
    public abstract Set<String> warnSpecificMessages();

    @NotNull
    public abstract Set<String> warnGeneralMessages();

    @NotNull
    public abstract Set<String> undeterminedSpecificMessages();

    @NotNull
    public abstract Set<String> undeterminedGeneralMessages();

    @NotNull
    public abstract Set<String> failSpecificMessages();

    @NotNull
    public abstract Set<String> failGeneralMessages();
}
