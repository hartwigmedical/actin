package com.hartwig.actin.clinical.feed;

import java.time.LocalDate;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FeedLine {

    @NotNull
    private final Map<String, Integer> fieldIndexMap;
    @NotNull
    private final String[] parts;

    FeedLine(@NotNull final Map<String, Integer> fieldIndexMap, @NotNull final String[] parts) {
        this.fieldIndexMap = fieldIndexMap;
        this.parts = parts;
    }

    @NotNull
    public String string(@NotNull String column) {
        return parts[fieldIndexMap.get(column)];
    }

    @NotNull
    public LocalDate date(@NotNull String column) {
        return FeedUtil.parseDate(parts[fieldIndexMap.get(column)]);
    }

    @NotNull
    public LocalDate optionalDate(@NotNull String column) {
        return FeedUtil.parseOptionalDate(parts[fieldIndexMap.get(column)]);
    }

    public double number(@NotNull String column) {
        return FeedUtil.parseDouble(parts[fieldIndexMap.get(column)]);
    }

    @Nullable
    public Double optionalNumber(@NotNull String column) {
        return FeedUtil.parseOptionalDouble(parts[fieldIndexMap.get(column)]);
    }

    public int integer(@NotNull String column) {
        return Integer.parseInt(parts[fieldIndexMap.get(column)]);
    }
}
