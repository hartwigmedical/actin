package com.hartwig.actin.treatment.ctc;

import java.util.List;
import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class CTCDatabase {

    @NotNull
    public abstract List<CTCDatabaseEntry> entries();

    @NotNull
    public abstract Set<String> studyMETCsToIgnore();

    @NotNull
    public abstract Set<Integer> unmappedCohortIds();

}
