package com.hartwig.actin.molecular.orange.datamodel.purple;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class PurpleRecord {

    @NotNull
    public abstract PurpleFit fit();

    @NotNull
    public abstract PurpleCharacteristics characteristics();

    @NotNull
    public abstract Set<PurpleVariant> variants();

    @NotNull
    public abstract Set<PurpleCopyNumber> copyNumbers();
}
