package com.hartwig.actin.treatment.input.single;

import org.jetbrains.annotations.NotNull;

public enum TumorTypeCategory {
    CARCINOMA,
    ADENOCARCINOMA,
    SQUAMOUS_CELL_CARCINOMA,
    MELANOMA;

    @NotNull
    public String display() {
        return this.toString().replaceAll("_", " ").toLowerCase();
    }

    @NotNull
    public static TumorTypeCategory fromString(@NotNull String tumorTypeString) {
        return TumorTypeCategory.valueOf(tumorTypeString.trim().replaceAll(" ", "_").toUpperCase());
    }
}
