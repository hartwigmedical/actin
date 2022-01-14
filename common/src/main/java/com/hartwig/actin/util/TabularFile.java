package com.hartwig.actin.util;

import java.util.Map;

import com.google.common.collect.Maps;

import org.jetbrains.annotations.NotNull;

public final class TabularFile {

    private TabularFile() {
    }

    @NotNull
    public static Map<String, Integer> createFields(@NotNull String[] header) {
        Map<String, Integer> fields = Maps.newHashMap();

        for (int i = 0; i < header.length; ++i) {
            fields.put(header[i], i);
        }

        return fields;
    }
}
