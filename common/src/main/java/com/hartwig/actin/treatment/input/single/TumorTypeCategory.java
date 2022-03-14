package com.hartwig.actin.treatment.input.single;

import org.jetbrains.annotations.NotNull;

public enum TumorTypeCategory {
    CARCINOMA("305"),
    ADENOCARCINOMA("299"),
    SQUAMOUS_CELL_CARCINOMA("1749"),
    MELANOMA("1909");

    @NotNull
    private final String doid;

    TumorTypeCategory(@NotNull final String doid) {
        this.doid = doid;
    }

    @NotNull
    public String doid() {
        return doid;
    }

    @NotNull
    public String display() {
        return this.toString().replaceAll("_", " ").toLowerCase();
    }

    @NotNull
    public static TumorTypeCategory fromString(@NotNull String tumorTypeString) {
        return TumorTypeCategory.valueOf(tumorTypeString.trim().replaceAll(" ", "_").toUpperCase());
    }
}
