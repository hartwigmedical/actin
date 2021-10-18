package com.hartwig.actin.datamodel.clinical;

import org.jetbrains.annotations.NotNull;

public enum Gender {
    MALE("Male"),
    FEMALE("Female");

    @NotNull
    private final String display;

    Gender(@NotNull final String display) {
        this.display = display;
    }

    @NotNull
    public String display() {
        return display;
    }
}
