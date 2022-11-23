package com.hartwig.actin.report.interpretation;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class PriorMolecularTestKey {

    @NotNull
    public abstract String test();

    @NotNull
    public abstract String scoreText();
}
