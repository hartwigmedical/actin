package com.hartwig.actin.clinical.datamodel;

import com.hartwig.actin.Displayable;

import org.jetbrains.annotations.NotNull;

public enum MedicationStatus implements Displayable {
    ACTIVE("Active"),
    ON_HOLD("On hold"),
    CANCELLED("Cancelled"),
    UNKNOWN("Unknown");

    @NotNull
    private final String display;

    MedicationStatus(@NotNull final String display) {
        this.display = display;
    }

    @Override
    @NotNull
    public String display() {
        return display;
    }
}
