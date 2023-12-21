package com.hartwig.actin.clinical.datamodel;

import com.hartwig.actin.Displayable;

import org.jetbrains.annotations.NotNull;

public enum Gender implements Displayable {
    MALE("Male"),
    FEMALE("Female");

    @NotNull
    private final String display;

    Gender(@NotNull final String display) {
        this.display = display;
    }

    @Override
    @NotNull
    public String display() {
        return display;
    }
}
