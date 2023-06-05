package com.hartwig.actin.molecular.orange.datamodel.purple;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class PurpleFit {

    public abstract boolean hasSufficientQuality();

    public abstract boolean containsTumorCells();

    public abstract double purity();

    public abstract double ploidy();

    public abstract Set<PurpleQCStatus> qcStatuses();
}
