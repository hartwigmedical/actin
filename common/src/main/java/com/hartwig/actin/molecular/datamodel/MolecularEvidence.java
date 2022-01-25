package com.hartwig.actin.molecular.datamodel;

import com.google.common.collect.Multimap;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class MolecularEvidence {

    @NotNull
    public abstract Multimap<String, String> actinTrialEvidence();

    @NotNull
    public abstract Multimap<String, String> generalTrialEvidence();

    @NotNull
    public abstract Multimap<String, String> generalResponsiveEvidence();

    @NotNull
    public abstract Multimap<String, String> generalResistanceEvidence();

}
