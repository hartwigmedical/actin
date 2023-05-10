package com.hartwig.actin.clinical.datamodel;

import java.util.Collections;
import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Surgery implements Treatment {

    @NotNull
    public Set<TreatmentType> types() {
        return Collections.emptySet();
    }

    @NotNull
    public Set<TreatmentCategory> categories() {
        return Set.of(TreatmentCategory.SURGERY);
    }

    public boolean isSystemic() {
        return false;
    }
}
