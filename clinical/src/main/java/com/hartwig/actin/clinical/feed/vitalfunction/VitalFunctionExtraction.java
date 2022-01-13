package com.hartwig.actin.clinical.feed.vitalfunction;

import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class VitalFunctionExtraction {

    private VitalFunctionExtraction() {
    }

    @NotNull
    public static VitalFunctionCategory determineCategory(@NotNull String string) {
        VitalFunctionCategory category = toCategory(string);
        if (category == null) {
            throw new IllegalStateException("Could not determine category for vital function: " + string);
        }
        return category;
    }

    @Nullable
    public static VitalFunctionCategory toCategory(@NotNull String string) {
        switch (string) {
            case "NIBP":
            case "NIBPLILI":
            case "NIBPLIRE":
                return VitalFunctionCategory.NON_INVASIVE_BLOOD_PRESSURE;
            case "ABP":
                return VitalFunctionCategory.ARTERIAL_BLOOD_PRESSURE;
            case "HR":
                return VitalFunctionCategory.HEART_RATE;
            default:
                return null;
        }
    }
}
