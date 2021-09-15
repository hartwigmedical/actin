package com.hartwig.actin.clinical.feed;

import java.util.List;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Feed {

    @NotNull
    public abstract List<PatientEntry> patientEntries();

    @NotNull
    public abstract List<LabEntry> labEntries();
}
