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

    @NotNull
    public abstract Set<String> passMessages();

    @NotNull
    public abstract Set<String> undeterminedMessages();

    @NotNull
    public abstract Set<String> failMessages();
}
