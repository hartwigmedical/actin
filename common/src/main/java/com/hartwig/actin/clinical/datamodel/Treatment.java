package com.hartwig.actin.clinical.datamodel;

import java.util.Set;

import org.jetbrains.annotations.NotNull;

public interface Treatment {

    @NotNull
    String name();

    @NotNull
    Set<TreatmentCategory> categories();

    @NotNull
    Set<String> synonyms();

    boolean isSystemic();
}