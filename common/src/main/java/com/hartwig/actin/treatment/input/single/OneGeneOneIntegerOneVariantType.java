package com.hartwig.actin.treatment.input.single;

import com.hartwig.actin.treatment.input.datamodel.VariantTypeInput;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class })
public abstract class OneGeneOneIntegerOneVariantType {

    @NotNull
    public abstract String geneName();

    public abstract int integer();

    @NotNull
    public abstract VariantTypeInput variantType();
}
