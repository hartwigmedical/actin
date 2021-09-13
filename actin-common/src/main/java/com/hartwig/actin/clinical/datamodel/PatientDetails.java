package com.hartwig.actin.clinical.datamodel;

import java.time.LocalDate;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class PatientDetails {

    @NotNull
    public abstract String sex();

    public abstract int birthYear();

    @NotNull
    public abstract LocalDate registrationDate();

    @NotNull
    public abstract LocalDate questionnaireDate();

}
