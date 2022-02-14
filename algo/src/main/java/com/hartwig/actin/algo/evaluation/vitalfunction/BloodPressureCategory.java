package com.hartwig.actin.algo.evaluation.vitalfunction;

import org.jetbrains.annotations.NotNull;

public enum BloodPressureCategory {
    SYSTOLIC("Systolic blood pressure"),
    DIASTOLIC("Diastolic blood pressure");

    @NotNull
    private final String display;

    BloodPressureCategory(@NotNull final String display) {
        this.display = display;
    }

    @NotNull
    public String display() {
        return display;
    }
}
