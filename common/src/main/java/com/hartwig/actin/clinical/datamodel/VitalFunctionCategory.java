package com.hartwig.actin.clinical.datamodel;

import org.jetbrains.annotations.NotNull;

public enum VitalFunctionCategory {
    BLOOD_PRESSURE("Blood pressure"),
    HEART_RATE("Heart rate");

    @NotNull
    private final String display;

    VitalFunctionCategory(@NotNull final String display) {
        this.display = display;
    }

    @NotNull
    public String display() {
        return display;
    }
}
