package com.hartwig.actin.treatment.input.datamodel;

import com.hartwig.actin.Displayable;

import org.jetbrains.annotations.NotNull;

public enum TumorTypeInput implements Displayable {
    CARCINOMA("305"),
    ADENOCARCINOMA("299"),
    SQUAMOUS_CELL_CARCINOMA("1749"),
    MELANOMA("1909");

    @NotNull
    private final String doid;

    TumorTypeInput(@NotNull final String doid) {
        this.doid = doid;
    }

    @NotNull
    public String doid() {
        return doid;
    }

    @Override
    @NotNull
    public String display() {
        return this.toString().replaceAll("_", " ").toLowerCase();
    }

    @NotNull
    public static TumorTypeInput fromString(@NotNull String string) {
        return TumorTypeInput.valueOf(string.trim().replaceAll(" ", "_").toUpperCase());
    }
}
