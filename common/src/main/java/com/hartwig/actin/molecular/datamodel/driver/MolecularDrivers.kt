package com.hartwig.actin.molecular.datamodel.driver;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class MolecularDrivers {

    @NotNull
    public abstract Set<Variant> variants();

    @NotNull
    public abstract Set<CopyNumber> copyNumbers();

    @NotNull
    public abstract Set<HomozygousDisruption> homozygousDisruptions();

    @NotNull
    public abstract Set<Disruption> disruptions();

    @NotNull
    public abstract Set<Fusion> fusions();

    @NotNull
    public abstract Set<Virus> viruses();
}
