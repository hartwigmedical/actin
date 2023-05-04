package com.hartwig.actin.clinical.datamodel;

import java.util.List;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Drug {

    @NotNull
    public abstract String name();

    @NotNull
    public abstract List<DrugClass> drugClasses();

    @NotNull
    public abstract List<String> synonyms();
}
