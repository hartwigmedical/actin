package com.hartwig.actin.clinical.feed.patient;

import java.time.LocalDate;

import com.hartwig.actin.clinical.datamodel.Gender;
import com.hartwig.actin.clinical.feed.FeedEntry;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class PatientEntry implements FeedEntry {

    @NotNull
    public abstract String id();

    @NotNull
    @Override
    public abstract String subject();

    public abstract int birthYear();

    @NotNull
    public abstract Gender gender();

    @NotNull
    public abstract LocalDate periodStart();

    @Nullable
    public abstract LocalDate periodEnd();

}
