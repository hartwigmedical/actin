package com.hartwig.actin.util;

import java.io.File;

import org.jetbrains.annotations.NotNull;

public final class FileUtil {

    private FileUtil() {
    }

    @NotNull
    public static String appendFileSeparator(@NotNull String path) {
        return path.endsWith(File.separator) ? path : path + File.separator;
    }
}
