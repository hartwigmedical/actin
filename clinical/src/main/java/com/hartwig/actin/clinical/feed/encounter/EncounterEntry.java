package com.hartwig.actin.clinical.feed.encounter;

import java.time.LocalDate;

import com.hartwig.actin.clinical.feed.FeedEntry;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class EncounterEntry implements FeedEntry {

    @NotNull
    @Override
    public abstract String subject();

    @NotNull
    public abstract String classDisplay();

    @NotNull
    public abstract LocalDate periodStart();

    @NotNull
    public abstract LocalDate periodEnd();

    @NotNull
    public abstract String codeCodingDisplayOriginal();

    @NotNull
    public abstract String encounterStatus();

    @NotNull
    public abstract String procedureStatus();
}
