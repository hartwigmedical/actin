package com.hartwig.actin.clinical.feed.vitalfunction;

import java.time.LocalDate;

import com.hartwig.actin.clinical.feed.FeedEntry;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class VitalFunctionEntry implements FeedEntry {

    @NotNull
    @Override
    public abstract String subject();

    @NotNull
    public abstract LocalDate effectiveDateTime();

    @NotNull
    public abstract String codeDisplayOriginal();

    @NotNull
    public abstract String componentCodeDisplay();

    @NotNull
    public abstract String quantityUnit();

    public abstract double quantityValue();

}
