package com.hartwig.actin.treatment.input.single;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class })
public abstract class OneGeneTwoIntegers {

    @NotNull
    public abstract String geneName();

    public abstract int integer1();

    public abstract int integer2();
}
