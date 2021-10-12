package com.hartwig.actin.util;

import java.io.File;
import java.util.Map;

import com.google.common.collect.Maps;

import org.jetbrains.annotations.NotNull;

public final class FileUtil {

    private FileUtil() {
    }

    @NotNull
    public static String appendFileSeparator(@NotNull String path) {
        return path.endsWith(File.separator) ? path : path + File.separator;
    }

    @NotNull
    public static Map<String, Integer> createFieldIndexMap(@NotNull String[] header) {
        Map<String, Integer> fieldIndexMap = Maps.newHashMap();

        for (int i = 0; i < header.length; ++i) {
            fieldIndexMap.put(header[i], i);
        }

        return fieldIndexMap;
    }
}
