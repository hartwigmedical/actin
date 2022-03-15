package com.hartwig.actin.treatment.input.single;

import java.util.List;

import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class OneTypedTreatmentManyStringsOneInteger {

    @NotNull
    public abstract TreatmentCategory category();

    @NotNull
    public abstract List<String> strings();

    public abstract int integer();
}
