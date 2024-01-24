package com.hartwig.actin.clinical.datamodel;

import com.hartwig.actin.Displayable;

import org.jetbrains.annotations.NotNull;

public enum VitalFunctionCategory implements Displayable {
    NON_INVASIVE_BLOOD_PRESSURE("Non-invasive blood pressure"),
    ARTERIAL_BLOOD_PRESSURE("Arterial blood pressure"),
    HEART_RATE("Heart rate"),
    SPO2("SpO2"),
    UNKNOWN("Unknown");

    @NotNull
    private final String display;

    VitalFunctionCategory(@NotNull final String display) {
        this.display = display;
    }

    @Override
    @NotNull
    public String display() {
        return display;
    }

    public static VitalFunctionCategory fromString(@NotNull final String display) {
        for (VitalFunctionCategory category : VitalFunctionCategory.values()) {
            if (category.display().equals(display)) {
                return category;
            }
        }
        return UNKNOWN;
    }
}
