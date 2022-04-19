package com.hartwig.actin.molecular.datamodel.driver;

import org.jetbrains.annotations.NotNull;

public enum VariantDriverType {
    HOTSPOT("Hotspot"),
    BIALLELIC("Biallelic VUS"),
    VUS("VUS");

    @NotNull
    private final String display;

    VariantDriverType(@NotNull final String display) {
        this.display = display;
    }

    @NotNull
    public String display() {
        return display;
    }
}
