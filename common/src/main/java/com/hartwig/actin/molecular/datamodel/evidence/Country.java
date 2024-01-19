package com.hartwig.actin.molecular.datamodel.evidence;

import com.hartwig.actin.Displayable;

import org.jetbrains.annotations.NotNull;

public enum Country implements Displayable {
    NETHERLANDS("Netherlands"),
    BELGIUM("Belgium"),
    GERMANY("Germany"),
    OTHER("Other");

    @NotNull
    private final String display;

    Country(@NotNull final String display) {
        this.display = display;
    }

    @Override
    @NotNull
    public String display() {
        return display;
    }
}
