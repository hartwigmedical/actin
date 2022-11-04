package com.hartwig.actin.treatment.input.single;

import com.hartwig.actin.treatment.input.datamodel.VariantTypeInput;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class OneIntegerOneStringOneVariantType {

    public abstract int integer();

    @NotNull
    public abstract String string();

    @NotNull
    public abstract VariantTypeInput variantType();
}
