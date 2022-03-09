package com.hartwig.actin.algo.evaluation.util;

import java.util.StringJoiner;

import org.jetbrains.annotations.NotNull;

public final class Format {

    private Format() {
    }

    @NotNull
    public static String concat(@NotNull Iterable<String> strings) {
        StringJoiner joiner = new StringJoiner("; ");
        for (String string : strings) {
            joiner.add(string);
        }
        return joiner.toString();
    }
}
