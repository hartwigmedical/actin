package com.hartwig.actin.clinical.datamodel.treatment;

import java.util.Collections;
import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Radiotherapy implements Treatment {
    public final TreatmentClass treatmentClass = TreatmentClass.RADIOTHERAPY;

    @Override
    @Value.Default
    public boolean isSystemic() {
        return false;
    }

    @Override
    @NotNull
    public Set<TreatmentType> types() {
        RadiotherapyType radiotherapyType = radioType();
        return (radiotherapyType == null) ? Collections.emptySet() : Set.of(radiotherapyType);
    }

    @Override
    @NotNull
    public Set<TreatmentCategory> categories() {
        return Set.of(TreatmentCategory.RADIOTHERAPY);
    }

    @Nullable
    public abstract RadiotherapyType radioType();

    @Nullable
    public abstract Boolean isInternal();
}
