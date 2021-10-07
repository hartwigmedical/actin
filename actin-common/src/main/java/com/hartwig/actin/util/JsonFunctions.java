package com.hartwig.actin.util;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JsonFunctions {

    private static final Logger LOGGER = LogManager.getLogger(JsonFunctions.class);

    private JsonFunctions() {
    }

    @NotNull
    public static JsonObject object(@NotNull JsonObject object, @NotNull String field) {
        assert object.get(field).isJsonObject();
        return object.getAsJsonObject(field);
    }

    @NotNull
    public static JsonArray array(@NotNull JsonObject object, @NotNull String field) {
        assert object.get(field).isJsonArray();
        return object.getAsJsonArray(field);
    }

    @Nullable
    public static List<String> nullableStringList(@NotNull JsonObject object, @NotNull String field) {
        if (object.get(field).isJsonNull()) {
            return null;
        }

        return stringList(object, field);
    }

    @NotNull
    public static List<String> stringList(@NotNull JsonObject object, @NotNull String field) {
        assert object.has(field);

        if (object.get(field).isJsonNull()) {
            return Lists.newArrayList();
        }

        List<String> values = Lists.newArrayList();
        if (object.get(field).isJsonPrimitive()) {
            values.add(string(object, field));
        } else {
            assert object.get(field).isJsonArray();
            for (JsonElement element : object.getAsJsonArray(field)) {
                if (!element.isJsonPrimitive()) {
                    LOGGER.warn("Converting array value for {} into string for element {}", field, element);
                }
                values.add(element.getAsJsonPrimitive().getAsString());
            }
        }
        return values;
    }

    @Nullable
    public static String nullableString(@NotNull JsonObject object, @NotNull String field) {
        if (object.get(field).isJsonNull()) {
            return null;
        }

        return string(object, field);
    }

    @NotNull
    public static String string(@NotNull JsonObject object, @NotNull String field) {
        JsonElement element = object.get(field);
        if (!element.isJsonPrimitive()) {
            LOGGER.warn("Converting {} to string for element {}.", field, element);
        }
        return element.getAsJsonPrimitive().getAsString();
    }

    @Nullable
    public static Integer nullableInteger(@NotNull JsonObject object, @NotNull String field) {
        if (object.get(field).isJsonNull()) {
            return null;
        }

        return integer(object, field);
    }

    public static int integer(@NotNull JsonObject object, @NotNull String field) {
        JsonElement element = object.get(field);
        if (!element.isJsonPrimitive()) {
            LOGGER.warn("Converting {} to Integer for element {}.", field, element);
        }
        return element.getAsJsonPrimitive().getAsInt();
    }

    @Nullable
    public static LocalDate nullableDate(@NotNull JsonObject object, @NotNull String field) {
        if (object.get(field).isJsonNull()) {
            return null;
        }

        return date(object, field);
    }

    @NotNull
    public static LocalDate date(@NotNull JsonObject object, @NotNull String field) {
        JsonObject dateObject = object(object, field);

        return LocalDate.of(integer(dateObject, "year"), integer(dateObject, "month"), integer(dateObject, "day"));
    }

    @Nullable
    public static Boolean nullableBoolean(@NotNull JsonObject object, @NotNull String field) {
        if (object.get(field).isJsonNull()) {
            return null;
        }

        return Boolean.valueOf(string(object, field));
    }
}
