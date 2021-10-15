package com.hartwig.actin.datamodel;

import com.hartwig.actin.datamodel.clinical.ClinicalRecord;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class ActinRecord {

    @NotNull
    @Value.Derived
    public String sampleId() {
        return clinical().sampleId();
    }

    @NotNull
    public abstract ClinicalRecord clinical();
}
