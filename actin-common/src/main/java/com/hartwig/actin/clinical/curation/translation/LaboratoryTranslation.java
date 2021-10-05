package com.hartwig.actin.clinical.curation.translation;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class LaboratoryTranslation implements Translation {

    @NotNull
    public abstract String code();

    @NotNull
    public abstract String translatedCode();

    @NotNull
    public abstract String name();

    @NotNull
    public abstract String translatedName();

}
