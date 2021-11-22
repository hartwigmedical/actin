package com.hartwig.actin.algo.doid.datamodel;

import java.util.List;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(allParameters = true,
             passAnnotations = { NotNull.class, Nullable.class })
public abstract class LogicalDefinitionAxioms {

    @NotNull
    public abstract String definedClassId();

    @NotNull
    public abstract List<String> genusIds();

    @NotNull
    public abstract List<Restriction> restrictions();
}
