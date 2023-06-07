package com.hartwig.actin.util.json;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Json {

    private Json() {
    }

    @Nullable
    public static JsonObject optionalObject(@NotNull JsonObject object, @NotNull String field) {
        return object.has(field) ? object(object, field) : null;
    }

    @Nullable
    public static JsonObject nullableObject(@NotNull JsonObject object, @NotNull String field) {
        return !isNull(object, field) ? object(object, field) : null;
    }

    @NotNull
    public static JsonObject object(@NotNull JsonObject object, @NotNull String field) {
        return object.getAsJsonObject(field);
    }

    @Nullable
    public static JsonArray optionalArray(@NotNull JsonObject object, @NotNull String field) {
        return object.has(field) ? array(object, field) : null;
    }

    @Nullable
    public static JsonArray nullableArray(@NotNull JsonObject object, @NotNull String field) {
        return !isNull(object, field) ? array(object, field) : null;
    }

    @NotNull
    public static JsonArray array(@NotNull JsonObject object, @NotNull String field) {
        return object.getAsJsonArray(field);
    }

    @Nullable
    public static List<String> optionalStringList(@NotNull JsonObject object, @NotNull String field) {
        return object.has(field) ? stringList(object, field) : null;
    }

    @Nullable
    public static List<String> nullableStringList(@NotNull JsonObject object, @NotNull String field) {
        return !isNull(object, field) ? stringList(object, field) : null;
    }

    @NotNull
    public static List<String> stringList(@NotNull JsonObject object, @NotNull String field) {
        List<String> values = Lists.newArrayList();
        if (object.get(field).isJsonPrimitive()) {
            values.add(string(object, field));
        } else {
            assert object.get(field).isJsonArray();
            for (JsonElement element : object.getAsJsonArray(field)) {
                values.add(element.getAsJsonPrimitive().getAsString());
            }
        }
        return values;
    }

    @Nullable
    public static List<Integer> nullableIntegerList(@NotNull JsonObject object, @NotNull String field) {
        return !isNull(object, field) ? integerList(object, field) : null;
    }

    @NotNull
    public static List<Integer> integerList(@NotNull JsonObject object, @NotNull String field) {
        List<Integer> values = Lists.newArrayList();
        if (object.get(field).isJsonPrimitive()) {
            values.add(integer(object, field));
        } else {
            assert object.get(field).isJsonArray();
            for (JsonElement element : object.getAsJsonArray(field)) {
                values.add(element.getAsJsonPrimitive().getAsInt());
            }
        }
        return values;
    }

    @Nullable
    public static String optionalString(@NotNull JsonObject object, @NotNull String field) {
        return object.has(field) ? string(object, field) : null;
    }

    @Nullable
    public static String nullableString(@NotNull JsonObject object, @NotNull String field) {
        return !isNull(object, field) ? string(object, field) : null;
    }

    @NotNull
    public static String string(@NotNull JsonObject object, @NotNull String field) {
        return object.get(field).getAsJsonPrimitive().getAsString();
    }

    @Nullable
    public static Double optionalNumber(@NotNull JsonObject object, @NotNull String field) {
        return object.has(field) ? nullableNumber(object, field) : null;
    }

    @Nullable
    public static Double nullableNumber(@NotNull JsonObject object, @NotNull String field) {
        return !isNull(object, field) ? number(object, field) : null;
    }

    public static double number(@NotNull JsonObject object, @NotNull String field) {
        return object.get(field).getAsJsonPrimitive().getAsDouble();
    }

    @Nullable
    public static Integer nullableInteger(@NotNull JsonObject object, @NotNull String field) {
        return !isNull(object, field) ? integer(object, field) : null;
    }

    public static int integer(@NotNull JsonObject object, @NotNull String field) {
        return object.get(field).getAsJsonPrimitive().getAsInt();
    }

    @Nullable
    public static Boolean optionalBool(@NotNull JsonObject object, @NotNull String field) {
        return object.has(field) ? nullableBool(object, field) : null;
    }

    @Nullable
    public static Boolean nullableBool(@NotNull JsonObject object, @NotNull String field) {
        return !isNull(object, field) ? bool(object, field) : null;
    }

    public static boolean bool(@NotNull JsonObject object, @NotNull String field) {
        return object.get(field).getAsJsonPrimitive().getAsBoolean();
    }

    @Nullable
    public static LocalDate nullableDate(@NotNull JsonObject object, @NotNull String field) {
        return !isNull(object, field) ? date(object, field) : null;
    }

    @NotNull
    public static LocalDate date(@NotNull JsonObject object, @NotNull String field) {
        JsonObject dateObject = object(object, field);

        return LocalDate.of(integer(dateObject, "year"), integer(dateObject, "month"), integer(dateObject, "day"));
    }

    private static boolean isNull(@NotNull JsonObject object, @NotNull String field) {
        return object.get(field).isJsonNull();
    }

    @NotNull
    public static <T> List<T> extractListFromJson(@NotNull JsonArray jsonArray, Function<JsonObject, T> extractor) {
        return jsonArray.asList().stream().map(JsonElement::getAsJsonObject).map(extractor).collect(Collectors.toList());
    }

    @NotNull
    public static <T> Set<T> extractSetFromJson(@NotNull JsonArray jsonArray, Function<JsonObject, T> extractor) {
        return jsonArray.asList().stream().map(JsonElement::getAsJsonObject).map(extractor).collect(Collectors.toSet());
    }
}
