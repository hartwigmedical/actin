package com.hartwig.actin.treatment.input.single;

import java.util.List;

import com.hartwig.actin.treatment.input.datamodel.TreatmentInputWithName;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class ManyTreatmentsWithName {

    @NotNull
    public abstract List<TreatmentInputWithName> treatmentsWithName();
}
