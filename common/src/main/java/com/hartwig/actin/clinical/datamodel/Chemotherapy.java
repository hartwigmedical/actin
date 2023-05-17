package com.hartwig.actin.clinical.datamodel;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Chemotherapy implements Therapy {

    public final TreatmentType treatmentType = TreatmentType.CHEMOTHERAPY;

    public abstract int maxCycles();
}
