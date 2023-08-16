package com.hartwig.actin.clinical.datamodel.treatment;

import java.util.Set;

import com.hartwig.actin.Displayable;

import org.jetbrains.annotations.NotNull;

public interface Treatment extends Displayable {

    @NotNull
    String name();

    @NotNull
    Set<TreatmentCategory> categories();

    @NotNull
    Set<TreatmentType> types();

    @NotNull
    Set<String> synonyms();

    boolean isSystemic();

    @NotNull
    @Override
    default String display() {
        return name().replace("_", " ").toLowerCase();
    }
}