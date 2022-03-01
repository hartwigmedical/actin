package com.hartwig.actin.algo.evaluation.laboratory;

import java.util.List;

import com.google.common.collect.Lists;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

enum LabUnit {
    GRAM_PER_LITER("g/L"),
    GRAM_PER_DECILITER("g/dL"),
    MICROMOL_PER_LITER("umol/L"),
    MILLIMOL_PER_LITER("mmol/L"),
    MILLIGRAM_PER_DECILITER("mg/dL"),
    CELLS_PER_MICROLITER("cells/mm3"),
    BILLION_PER_LITER("10^9/L", "10*9/L");

    @Nullable
    public static LabUnit fromString(@NotNull String displayToMatch) {
        for (LabUnit labUnit : LabUnit.values()) {
            for (String display : labUnit.displays) {
                if (display.equals(displayToMatch)) {
                    return labUnit;
                }
            }
        }

        return null;
    }

    @NotNull
    private final List<String> displays;

    LabUnit(@NotNull final String... displays) {
        this.displays = Lists.newArrayList(displays);
    }

    @NotNull
    public String display() {
        return displays.get(0);
    }
}
