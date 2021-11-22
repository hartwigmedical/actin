package com.hartwig.actin.algo.doid.datamodel;

import java.util.List;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(allParameters = true,
             passAnnotations = { NotNull.class, Nullable.class })
public abstract class DoidEntry {

    @NotNull
    public abstract String id();

    @NotNull
    public abstract List<Node> nodes();

    @NotNull
    public abstract List<Edge> edges();

    @NotNull
    public abstract GraphMetadata metadata();

    @Nullable
    public abstract List<LogicalDefinitionAxioms> logicalDefinitionAxioms();

    @Nullable
    public abstract List<String> equivalentNodesSets();

    @Nullable
    public abstract List<String> domainRangeAxioms();

    @Nullable
    public abstract List<String> propertyChainAxioms();

}
