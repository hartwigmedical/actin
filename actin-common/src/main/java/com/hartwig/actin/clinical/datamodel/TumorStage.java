package com.hartwig.actin.clinical.datamodel;

import org.jetbrains.annotations.NotNull;

public enum TumorStage {
    I("I"),
    II("II"),
    IIB("IIb"),
    III("III"),
    IIIC("IIIc"),
    IV("IV");

    @NotNull
    private final String display;

    TumorStage(@NotNull final String display) {
        this.display = display;
    }

    @NotNull
    public String display() {
        return display;
    }
}
