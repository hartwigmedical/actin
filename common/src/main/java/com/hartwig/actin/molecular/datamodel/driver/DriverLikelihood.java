package com.hartwig.actin.molecular.datamodel.driver;

import org.jetbrains.annotations.NotNull;

public enum DriverLikelihood {
    HIGH("High"),
    LOW("Low");

    @NotNull
    private final String display;

    DriverLikelihood(@NotNull final String display) {
        this.display = display;
    }

    @NotNull
    public String display() {
        return display;
    }
}
