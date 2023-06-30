package com.hartwig.actin.clinical.datamodel.treatment;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class TargetedTherapy implements Therapy {

    @Override
    @NotNull
    @Value.Default
    public Set<TreatmentCategory> categories() {
        return Set.of(TreatmentCategory.TARGETED_THERAPY);
    }
}
