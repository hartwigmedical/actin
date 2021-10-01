package com.hartwig.actin.database.dao;

import java.util.StringJoiner;

import org.jetbrains.annotations.Nullable;

final class DataUtil {

    private DataUtil() {
    }

    @Nullable
    public static Byte toByte(@Nullable Boolean bool) {
        return bool != null ? (byte) (bool ? 1 : 0) : null;
    }

    @Nullable
    public static String concat(@Nullable Iterable<String> strings) {
        if (strings == null) {
            return null;
        }

        StringJoiner joiner = new StringJoiner(";");
        for (String entry : strings) {
            joiner.add(entry);
        }
        return joiner.toString();
    }
}

