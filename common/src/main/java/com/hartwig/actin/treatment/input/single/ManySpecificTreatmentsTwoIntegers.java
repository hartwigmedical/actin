package com.hartwig.actin.treatment.input.single;

import java.util.List;

import com.hartwig.actin.clinical.datamodel.treatment.Treatment;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class ManySpecificTreatmentsTwoIntegers {

    public abstract int integer1();

    public abstract int integer2();

    @NotNull
    public abstract List<Treatment> treatments();
}
