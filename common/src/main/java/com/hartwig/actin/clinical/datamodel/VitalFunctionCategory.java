package com.hartwig.actin.clinical.datamodel;

import org.jetbrains.annotations.NotNull;

public enum VitalFunctionCategory {
    NON_INVASIVE_BLOOD_PRESSURE("Non-invasive blood pressure"),
    ARTERIAL_BLOOD_PRESSURE("Arterial blood pressure"),
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
