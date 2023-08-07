package com.hartwig.actin.clinical.datamodel.treatment;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = {NotNull.class, Nullable.class})
public abstract class OtherTreatment implements Treatment {
    public final TreatmentType treatmentType = TreatmentType.OTHER_TREATMENT;
}
