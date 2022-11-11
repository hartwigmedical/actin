package com.hartwig.actin.molecular.datamodel.driver;

import org.jetbrains.annotations.NotNull;

public enum FusionDriverType {
    KNOWN_PAIR("Known fusion"),
    KNOWN_PAIR_IG("IG known fusion"),
    KNOWN_PAIR_DEL_DUP("Known fusion"),
    PROMISCUOUS_3("3' promiscuous fusion"),
    PROMISCUOUS_5("5' promiscuous fusion"),
    PROMISCUOUS_BOTH("3' and 5' promiscuous fusion"),
    PROMISCUOUS_IG("IG promiscuous fusion");

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
