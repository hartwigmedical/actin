package com.hartwig.actin.clinical.datamodel.treatment;

import java.util.Set;
import java.util.stream.Collectors;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class DrugTherapy implements Therapy {
    public final TreatmentClass treatmentClass = TreatmentClass.DRUG_THERAPY;

    @Override
    @Value.Default
    public boolean isSystemic() {
        return true;
    }

    @Override
    @NotNull
    public Set<TreatmentCategory> categories() {
        return drugs().stream().map(Drug::category).collect(Collectors.toSet());
    }

    @Override
    @NotNull
    public Set<TreatmentType> types() {
        return drugs().stream().flatMap(drug -> drug.drugTypes().stream()).collect(Collectors.toSet());
    }
}
