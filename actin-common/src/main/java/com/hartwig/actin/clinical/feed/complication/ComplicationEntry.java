package com.hartwig.actin.clinical.feed.complication;

import java.time.LocalDate;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class ComplicationEntry {

    @NotNull
    public abstract String subject();

    @NotNull
    public abstract String identifierSystem();

    @NotNull
    public abstract String categoryCodeOriginal();

    @NotNull
    public abstract String categoryDisplay();

    @NotNull
    public abstract String categoryDisplayOriginal();

    @NotNull
    public abstract String clinicalStatus();

    @NotNull
    public abstract String codeCodeOriginal();

    @NotNull
    public abstract String codeDisplayOriginal();

    @NotNull
    public abstract String codeCode();

    @NotNull
    public abstract String codeDisplay();

    @NotNull
    public abstract LocalDate onsetPeriodStart();

    @Nullable
    public abstract LocalDate onsetPeriodEnd();

    @NotNull
    public abstract String severityCode();

    @NotNull
    public abstract String severityDisplay();

    @NotNull
    public abstract String severityDisplayNl();

    @NotNull
    public abstract String specialtyCodeOriginal();

    @NotNull
    public abstract String specialtyDisplayOriginal();

    @NotNull
    public abstract String verificationStatusCode();

}
