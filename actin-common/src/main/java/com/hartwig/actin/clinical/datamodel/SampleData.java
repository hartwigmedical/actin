package com.hartwig.actin.clinical.datamodel;

import java.util.List;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class SampleData {

    @NotNull
    public abstract String sampleId();

    @NotNull
    public abstract PatientDetails patient();

    @NotNull
    public abstract List<HistoryTumorTreatment> historyTumorTreatments();

}
