package com.hartwig.actin.clinical.util;

import java.util.Map;

import com.google.common.collect.Maps;

import org.jetbrains.annotations.NotNull;

public final class TsvUtil {

    private TsvUtil() {
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
