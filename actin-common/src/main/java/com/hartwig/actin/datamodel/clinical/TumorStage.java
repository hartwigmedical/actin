package com.hartwig.actin.datamodel.clinical;

import org.jetbrains.annotations.NotNull;

public enum TumorStage {
    I("I"),
    II("II"),
    IIA("IIA"),
    IIB("IIB"),
    III("III"),
    IIIA("IIIA"),
    IIIB("IIIB"),
    IIIC("IIIC"),
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
