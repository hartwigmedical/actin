package com.hartwig.actin.molecular.orange.datamodel.linx;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class LinxRecord {

    // TODO add hom disruptions and fusions

    @NotNull
    public abstract Set<GeneDisruption> disruptions();
}
