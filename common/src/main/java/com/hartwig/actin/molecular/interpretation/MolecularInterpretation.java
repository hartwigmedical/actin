package com.hartwig.actin.molecular.interpretation;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class MolecularInterpretation {

    @NotNull
    public abstract Set<String> applicableResponsiveEvents();

    @NotNull
    public abstract Set<String> applicableResistanceEvents();
}
