package com.hartwig.actin.clinical.curation.translation;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class AllergyTranslation implements Translation {

    @NotNull
    public abstract String name();

    @NotNull
    public abstract String translatedName();

}
