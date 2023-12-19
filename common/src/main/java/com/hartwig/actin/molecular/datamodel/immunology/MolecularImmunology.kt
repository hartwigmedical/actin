package com.hartwig.actin.molecular.datamodel.immunology;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class MolecularImmunology {

    public abstract boolean isReliable();

    @NotNull
    public abstract Set<HlaAllele> hlaAlleles();
}
