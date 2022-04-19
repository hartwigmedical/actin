package com.hartwig.actin.molecular.datamodel.driver;

import org.jetbrains.annotations.NotNull;

public enum FusionDriverType {
    KNOWN("Known fusion"),
    PROMISCUOUS("Promiscuous fusion");

    @NotNull
    private final String display;

    FusionDriverType(@NotNull final String display) {
        this.display = display;
    }

    @NotNull
    public String display() {
        return display;
    }
}
