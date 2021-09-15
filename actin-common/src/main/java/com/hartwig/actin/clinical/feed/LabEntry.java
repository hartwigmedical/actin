package com.hartwig.actin.clinical.feed;

import java.time.LocalDate;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class LabEntry {

    @NotNull
    public abstract String subject();

    @NotNull
    public abstract String codeCodeOriginal();

    @NotNull
    public abstract String codeDisplayOriginal();

    @NotNull
    public abstract LocalDate issued();

    @NotNull
    public abstract String valueQuantityComparator();

    public abstract double valueQuantityValue();

    @NotNull
    public abstract String valueQuantityUnit();

    @NotNull
    public abstract String interpretationDisplayOriginal();

    @Nullable
    public abstract String valueString();

    @Nullable
    public abstract String codeCode();

    @NotNull
    public abstract String referenceRangeText();

}
