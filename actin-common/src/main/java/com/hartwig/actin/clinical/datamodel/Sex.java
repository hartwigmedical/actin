package com.hartwig.actin.clinical.datamodel;

import org.jetbrains.annotations.NotNull;

public enum Sex {
    MALE("Male"),
    FEMALE("Female");

    @NotNull
    private final String display;

    Sex(@NotNull final String display) {
        this.display = display;
    }

    @NotNull
    public String display() {
        return display;
    }
}
