package com.hartwig.actin.clinical.feed.intolerance;

import java.time.LocalDate;

import com.hartwig.actin.clinical.feed.FeedEntry;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class IntoleranceEntry implements FeedEntry {

    @NotNull
    @Override
    public abstract String subject();

    @NotNull
    public abstract LocalDate assertedDate();

    @NotNull
    public abstract String category();

    @NotNull
    public abstract String categoryAllergyCategoryCode();

    @NotNull
    public abstract String categoryAllergyCategoryDisplay();

    @NotNull
    public abstract String clinicalStatus();

    @NotNull
    public abstract String codeText();

    @NotNull
    public abstract String criticality();
}
