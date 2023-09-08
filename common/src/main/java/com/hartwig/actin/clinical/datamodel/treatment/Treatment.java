package com.hartwig.actin.clinical.datamodel.treatment;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.hartwig.actin.Displayable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    @Nullable
    String displayOverride();

    @NotNull
    @Override
    default String display() {
        String alternateDisplay = displayOverride();
        return (alternateDisplay != null)
                ? alternateDisplay
                : Arrays.stream(name().replace("_", " ").split("\\+"))
                        .map(name -> (name.length() < 2) ? name : name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase())
                        .collect(Collectors.joining("+"));
    }
}