package com.hartwig.actin.molecular.orange.datamodel.lilac;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class LilacRecord {

    @NotNull
    public abstract String qc();

    @NotNull
    public abstract Set<LilacHlaAllele> alleles();
}
