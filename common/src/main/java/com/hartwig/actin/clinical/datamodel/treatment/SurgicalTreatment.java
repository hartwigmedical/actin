package com.hartwig.actin.clinical.datamodel.treatment;

import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class SurgicalTreatment implements Treatment {

    @Override
    @NotNull
    @Value.Default
    public Set<TreatmentCategory> categories() {
        return Set.of(TreatmentCategory.SURGERY);
    }

    public boolean isSystemic() {
        return false;
    }
}
