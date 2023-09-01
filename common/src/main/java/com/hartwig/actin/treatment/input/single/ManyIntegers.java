package com.hartwig.actin.treatment.input.single;

import java.util.List;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class ManyIntegers {

    @NotNull
    public abstract List<Integer> integers();
}
