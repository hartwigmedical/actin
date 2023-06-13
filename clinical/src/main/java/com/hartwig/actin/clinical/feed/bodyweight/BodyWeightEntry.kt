package com.hartwig.actin.clinical.feed.bodyweight;

import java.time.LocalDate;

import com.hartwig.actin.clinical.feed.FeedEntry;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class BodyWeightEntry implements FeedEntry {

    @NotNull
    @Override
    public abstract String subject();

    public abstract double valueQuantityValue();

    @NotNull
    public abstract String valueQuantityUnit();

    @NotNull
    public abstract LocalDate effectiveDateTime();

}
