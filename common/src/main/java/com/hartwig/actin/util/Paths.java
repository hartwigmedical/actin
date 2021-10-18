package com.hartwig.actin.util;

import java.io.File;

import org.jetbrains.annotations.NotNull;

public final class Paths {

    private Paths() {
    }

    @NotNull
    public static String forceTrailingFileSeparator(@NotNull String path) {
        return !path.endsWith(File.separator) ? path + File.separator : path;
    }
}
