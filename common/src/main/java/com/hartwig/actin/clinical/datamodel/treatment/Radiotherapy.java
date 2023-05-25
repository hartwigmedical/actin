package com.hartwig.actin.clinical.datamodel.treatment;

import java.util.Collections;
import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Radiotherapy implements Therapy {

    public final TreatmentType treatmentType = TreatmentType.RADIOTHERAPY;

    @NotNull
    public Set<Drug> drugs() {
        return Collections.emptySet();
    }

    @Nullable
    public abstract String radioType();

    @Nullable
    public abstract Boolean isInternal();
}
