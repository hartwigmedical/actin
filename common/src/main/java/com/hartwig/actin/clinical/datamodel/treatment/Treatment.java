package com.hartwig.actin.clinical.datamodel.treatment;

import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Treatment {

    @NotNull
    String name();

    @NotNull
    Set<TreatmentCategory> categories();

    @Nullable
    Set<TreatmentType> types();

    @NotNull
    Set<String> synonyms();

    boolean isSystemic();
}