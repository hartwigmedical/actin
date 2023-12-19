package com.hartwig.actin.clinical.datamodel.treatment.history;

import com.hartwig.actin.clinical.datamodel.treatment.Treatment;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public interface TreatmentStage {

    @NotNull
    Treatment treatment();

    @Nullable
    Integer cycles();

    @Nullable
    Integer startYear();

    @Nullable
    Integer startMonth();
}
