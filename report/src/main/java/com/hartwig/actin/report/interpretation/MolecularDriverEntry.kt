package com.hartwig.actin.report.interpretation;

import java.util.Set;

import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class MolecularDriverEntry {

    @NotNull
    public abstract String driverType();

    @NotNull
    public abstract String driver();

    @Nullable
    public abstract DriverLikelihood driverLikelihood();

    @NotNull
    public abstract Set<String> actinTrials();

    @NotNull
    public abstract Set<String> externalTrials();

    @Nullable
    public abstract String bestResponsiveEvidence();

    @Nullable
    public abstract String bestResistanceEvidence();

}
