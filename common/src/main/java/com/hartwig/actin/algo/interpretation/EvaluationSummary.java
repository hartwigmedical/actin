package com.hartwig.actin.algo.interpretation;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class EvaluationSummary {

    public abstract int count();

    public abstract int warningCount();

    public abstract int passedCount();

    public abstract int failedCount();

    public abstract int undeterminedCount();

    public abstract int nonImplementedCount();
}
