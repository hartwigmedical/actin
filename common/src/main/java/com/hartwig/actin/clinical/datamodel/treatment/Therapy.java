package com.hartwig.actin.clinical.datamodel.treatment;

import java.util.Set;

import org.jetbrains.annotations.NotNull;

public interface Therapy extends Treatment {

    @NotNull
    Set<Drug> drugs();
}
