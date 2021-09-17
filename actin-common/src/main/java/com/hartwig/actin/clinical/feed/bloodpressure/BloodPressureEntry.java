package com.hartwig.actin.clinical.feed.bloodpressure;

import java.time.LocalDate;

import com.hartwig.actin.clinical.feed.FeedEntry;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class BloodPressureEntry implements FeedEntry {

    @NotNull
    @Override
    public abstract String subject();

    @NotNull
    public abstract LocalDate effectiveDateTime();

    @NotNull
    public abstract String codeCodeOriginal();

    @NotNull
    public abstract String codeDisplayOriginal();

    @Nullable
    public abstract LocalDate issued();

    @NotNull
    public abstract String valueString();

    @NotNull
    public abstract String componentCodeCode();

    @NotNull
    public abstract String componentCodeDisplay();

    @NotNull
    public abstract String componentValueQuantityCode();

    public abstract double componentValueQuantityValue();

}
