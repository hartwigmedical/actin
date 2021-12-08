package com.hartwig.actin.algo.evaluation.laboratory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

enum LabUnit {
    G_PER_L("g/L"),
    G_PER_DL("g/dL"),
    MMOL_PER_L("mmol/L");

    @Nullable
    public static LabUnit fromString(@NotNull String displayToMatch) {
        for (LabUnit labUnit : LabUnit.values()) {
            if (labUnit.display().equals(displayToMatch)) {
                return labUnit;
            }
        }

        return null;
    }

    @NotNull
    private final String display;

    LabUnit(@NotNull final String display) {
        this.display = display;
    }

    @NotNull
    public String display() {
        return display;
    }
}
