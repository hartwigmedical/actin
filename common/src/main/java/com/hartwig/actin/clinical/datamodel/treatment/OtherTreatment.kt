package com.hartwig.actin.clinical.datamodel.treatment;

import java.util.Collections;
import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class OtherTreatment implements Treatment {
    public final TreatmentClass treatmentClass = TreatmentClass.OTHER_TREATMENT;

    @Override
    @Value.Default
    @NotNull
    public Set<TreatmentType> types() {
        return Collections.emptySet();
    }
}
