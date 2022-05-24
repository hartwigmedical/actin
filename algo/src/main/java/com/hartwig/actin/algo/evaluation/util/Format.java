package com.hartwig.actin.algo.evaluation.util;

import java.util.Set;
import java.util.StringJoiner;

import com.google.common.collect.Sets;

import org.jetbrains.annotations.NotNull;

public final class Format {

    private Format() {
    }

    @NotNull
    public static String concat(@NotNull Iterable<String> strings) {
        Set<String> unique = Sets.newHashSet(strings);

        StringJoiner joiner = new StringJoiner("; ");
        for (String string : unique) {
            joiner.add(string);
        }
        return joiner.toString();
    }
}
