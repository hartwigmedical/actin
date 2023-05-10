package com.hartwig.actin.clinical.datamodel;

import java.util.Set;

import com.hartwig.actin.treatment.datamodel.EligibilityFunction;

import org.jetbrains.annotations.NotNull;

public interface Therapy extends Treatment {

    boolean isOptional();

    int score();

    @NotNull
    Set<EligibilityFunction> eligibilityFunctions();

    @NotNull
    Set<Integer> lines();

    @NotNull
    Set<Drug> drugs();
}
