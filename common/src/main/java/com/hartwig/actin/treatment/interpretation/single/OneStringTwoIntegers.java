package com.hartwig.actin.treatment.interpretation.single;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class OneStringTwoIntegers {

    @NotNull
    public abstract String string();

    public abstract int integer1();

    public abstract int integer2();
}
