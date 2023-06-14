package com.hartwig.actin.clinical.datamodel.treatment.history;

import java.time.LocalDate;

import com.hartwig.actin.clinical.datamodel.SurgeryStatus;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class SurgeryHistoryDetails {

    @NotNull
    public abstract LocalDate endDate();

    @NotNull
    public abstract SurgeryStatus status();
}
