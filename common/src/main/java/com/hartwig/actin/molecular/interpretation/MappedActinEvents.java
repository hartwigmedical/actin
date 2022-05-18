package com.hartwig.actin.molecular.interpretation;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class MappedActinEvents {

    @NotNull
    public abstract Set<GeneMutation> mutations();

    @NotNull
    public abstract Set<String> activatedGenes();

    @NotNull
    public abstract Set<String> inactivatedGenes();

    @NotNull
    public abstract Set<String> amplifiedGenes();

    @NotNull
    public abstract Set<String> wildtypeGenes();

    @NotNull
    public abstract Set<FusionGene> fusions();
}
