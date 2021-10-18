package com.hartwig.actin.clinical.feed;

import java.time.LocalDate;
import java.util.Map;

import com.hartwig.actin.datamodel.clinical.Gender;

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
        assert fieldIndexMap.containsKey(column);
        return parts[fieldIndexMap.get(column)];
    }

    @NotNull
    public Gender gender(@NotNull String column) {
        return FeedParseFunctions.parseGender(string(column));
    }

    @NotNull
    public LocalDate date(@NotNull String column) {
        return FeedParseFunctions.parseDate(string(column));
    }

    @Nullable
    public LocalDate optionalDate(@NotNull String column) {
        return FeedParseFunctions.parseOptionalDate(string(column));
    }

    public double number(@NotNull String column) {
        return FeedParseFunctions.parseDouble(string(column));
    }

    @Nullable
    public Double optionalNumber(@NotNull String column) {
        return FeedParseFunctions.parseOptionalDouble(string(column));
    }

    public int integer(@NotNull String column) {
        return Integer.parseInt(string(column));
    }
}
