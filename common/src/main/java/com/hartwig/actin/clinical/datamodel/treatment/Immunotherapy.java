package com.hartwig.actin.clinical.datamodel.treatment;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Immunotherapy implements Therapy {

    public final TreatmentType treatmentType = TreatmentType.IMMUNOTHERAPY;
}
