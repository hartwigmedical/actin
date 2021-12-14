package com.hartwig.actin.treatment.interpretation.single;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class TwoDoubleInput {

    public abstract double double1();

    public abstract double double2();

}
