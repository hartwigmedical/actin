package com.hartwig.actin.molecular.datamodel.driver;

import com.hartwig.actin.Displayable;

import org.jetbrains.annotations.NotNull;

public enum FusionDriverType implements Displayable {
    NONE("None"),
    KNOWN_PAIR("Known fusion"),
    KNOWN_PAIR_IG("IG known fusion"),
    KNOWN_PAIR_DEL_DUP("Known fusion"),
    PROMISCUOUS_3("3' promiscuous fusion"),
    PROMISCUOUS_5("5' promiscuous fusion"),
    PROMISCUOUS_BOTH("3' and 5' promiscuous fusion"),
    PROMISCUOUS_IG("IG promiscuous fusion"),
    PROMISCUOUS_ENHANCER_TARGET("Promiscuous enhancer target fusion");

    @NotNull
    private final String display;

    FusionDriverType(@NotNull final String display) {
        this.display = display;
    }

    @Override
    @NotNull
    public String display() {
        return display;
    }
}
