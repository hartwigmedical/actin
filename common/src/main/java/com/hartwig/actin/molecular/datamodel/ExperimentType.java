package com.hartwig.actin.molecular.datamodel;

import com.hartwig.actin.Displayable;

import org.jetbrains.annotations.NotNull;

public enum ExperimentType implements Displayable {
    TARGETED("Panel analysis"),
    WHOLE_GENOME("WGS");

    @NotNull
    private final String display;

    ExperimentType(@NotNull final String display) { this.display = display; }

    @Override
    @NotNull
    public String display() { return display; }
}