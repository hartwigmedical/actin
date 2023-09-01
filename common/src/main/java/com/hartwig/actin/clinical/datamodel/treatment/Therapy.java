package com.hartwig.actin.clinical.datamodel.treatment;

import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Therapy extends Treatment {

    @NotNull
    Set<Drug> drugs();

    @Nullable
    Integer maxCycles();
}

