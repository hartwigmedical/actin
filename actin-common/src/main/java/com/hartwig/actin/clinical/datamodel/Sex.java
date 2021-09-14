package com.hartwig.actin.clinical.datamodel;

import org.jetbrains.annotations.NotNull;

public enum Sex {
    MALE,
    FEMALE;

    @NotNull
    public static Sex parseSex(@NotNull String sex) {
        if (sex.equalsIgnoreCase("male")) {
            return MALE;
        } else if (sex.equalsIgnoreCase("female")) {
            return FEMALE;
        }

        throw new IllegalArgumentException("Could not resolve sex: " + sex);
    }
}
