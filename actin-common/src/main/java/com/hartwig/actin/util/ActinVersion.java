package com.hartwig.actin.util;

import org.jetbrains.annotations.NotNull;

public final class ActinVersion {

    private ActinVersion() {
    }

    @NotNull
    public static String version() {
        return "0.1";
    }
}
