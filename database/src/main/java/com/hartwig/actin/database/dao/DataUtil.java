package com.hartwig.actin.database.dao;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class DataUtil {

    private static final String SEPARATOR = ";";

    private DataUtil() {
    }

    @Nullable
    public static Byte toByte(@Nullable Boolean bool) {
        return bool != null ? (byte) (bool ? 1 : 0) : null;
    }

    @Nullable
    public static String concat(@Nullable Collection<String> strings) {
        return (strings == null) ? null : concatStream(strings.stream());
    }

    @NotNull
    public static <T> String concatObjects(@NotNull Collection<T> objects) {
        return concatStream(objects.stream().map(Object::toString));
    }

    @NotNull
    public static String concatStream(@NotNull Stream<String> stream) {
        return stream.collect(Collectors.joining(SEPARATOR));
    }

    @Nullable
    public static String nullableToString(@Nullable Object object) {
        return object != null ? object.toString() : null;
    }
}

