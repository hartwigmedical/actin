package com.hartwig.actin.util.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class JsonTest {

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canExtractObjects() {
        JsonObject object = new JsonObject();

        assertNull(Json.optionalObject(object, "object"));

        object.add("object", new JsonObject());
        assertNotNull(Json.optionalObject(object, "object"));
        assertNotNull(Json.object(object, "object"));

        object.add("null", null);
        assertNull(Json.nullableObject(object, "null"));
    }

    @Test
    public void canExtractArrays() {
        JsonObject object = new JsonObject();

        assertNull(Json.optionalArray(object, "array1"));

        object.add("array1", new JsonArray());
        assertNotNull(Json.nullableArray(object, "array1"));
        assertNotNull(Json.optionalArray(object, "array1"));
        assertNotNull(Json.array(object, "array1"));

        object.add("array2", JsonNull.INSTANCE);
        assertNull(Json.nullableArray(object, "array2"));
    }

    @Test
    public void canExtractStringLists() {
        JsonObject object = new JsonObject();

        object.addProperty("nullable", (String) null);
        assertNull(Json.optionalStringList(object, "array1"));
        assertNull(Json.nullableStringList(object, "nullable"));

        JsonArray array = new JsonArray();
        array.add("value1");
        array.add("value2");
        object.add("array1", array);
        assertEquals(2, Json.optionalStringList(object, "array1").size());
        assertEquals(2, Json.nullableStringList(object, "array1").size());
        assertEquals(2, Json.stringList(object, "array1").size());

        object.addProperty("string", "string");
        assertEquals(1, Json.nullableStringList(object, "string").size());
    }

    @Test
    public void canExtractIntegerLists() {
        JsonObject object = new JsonObject();

        object.addProperty("nullable", (Integer) null);
        assertNull(Json.nullableIntegerList(object, "nullable"));

        JsonArray array = new JsonArray();
        array.add(1);
        array.add(2);
        object.add("array1", array);
        assertEquals(2, Json.nullableIntegerList(object, "array1").size());
        assertEquals(2, Json.integerList(object, "array1").size());

        object.addProperty("integer", 1);
        assertEquals(1, Json.nullableIntegerList(object, "integer").size());
    }

    @Test
    public void canExtractStrings() {
        JsonObject object = new JsonObject();

        assertNull(Json.optionalString(object, "string"));

        object.addProperty("string", "value");
        assertEquals("value", Json.optionalString(object, "string"));
        assertEquals("value", Json.nullableString(object, "string"));
        assertEquals("value", Json.string(object, "string"));

        object.addProperty("nullable", (String) null);
        assertNull(Json.nullableString(object, "nullable"));
    }

    @Test
    public void canExtractNumbers() {
        JsonObject object = new JsonObject();

        assertNull(Json.optionalNumber(object, "number"));

        object.addProperty("nullable", (String) null);
        assertNull(Json.nullableNumber(object, "nullable"));

        object.addProperty("number", 12.4);
        assertEquals(12.4, Json.nullableNumber(object, "number"), EPSILON);
        assertEquals(12.4, Json.number(object, "number"), EPSILON);
    }

    @Test
    public void canExtractIntegers() {
        JsonObject object = new JsonObject();

        object.addProperty("nullable", (String) null);
        assertNull(Json.nullableInteger(object, "nullable"));

        object.addProperty("integer", 8);
        assertEquals(8, (int) Json.nullableInteger(object, "integer"));
        assertEquals(8, Json.integer(object, "integer"));
    }

    @Test
    public void canExtractBooleans() {
        JsonObject object = new JsonObject();

        assertNull(Json.optionalBool(object, "bool"));

        object.addProperty("nullable", (String) null);
        assertNull(Json.nullableBool(object, "nullable"));

        object.addProperty("bool", true);
        assertTrue(Json.nullableBool(object, "bool"));
        assertTrue(Json.bool(object, "bool"));
    }

    @Test
    public void canExtractDates() {
        JsonObject object = new JsonObject();

        object.addProperty("nullable", (String) null);
        assertNull(Json.nullableDate(object, "nullable"));

        JsonObject dateObject = new JsonObject();
        dateObject.addProperty("year", 2018);
        dateObject.addProperty("month", 4);
        dateObject.addProperty("day", 6);
        object.add("date", dateObject);

        assertEquals(LocalDate.of(2018, 4, 6), Json.nullableDate(object, "date"));
        assertEquals(LocalDate.of(2018, 4, 6), Json.date(object, "date"));
    }

    @Test
    public void shouldExtractEmptyJsonArrayToEmptyList() {
        assertEquals(Collections.emptyList(), Json.extractListFromJson(new JsonArray(), JsonTest::getIndex));
    }

    @Test
    public void shouldExtractJsonArrayToList() {
        List<Integer> list = List.of(3, 2, 1);
        assertEquals(list, Json.extractListFromJson(jsonArrayFromCollection(list), JsonTest::getIndex));
    }

    @Test
    public void shouldExtractEmptyJsonArrayToEmptySet() {
        assertEquals(Collections.emptySet(), Json.extractSetFromJson(new JsonArray(), JsonTest::getIndex));
    }

    @Test
    public void shouldExtractJsonArrayToSet() {
        Set<Integer> set = Set.of(3, 2, 1);
        assertEquals(set, Json.extractSetFromJson(jsonArrayFromCollection(set), JsonTest::getIndex));
    }

    @NotNull
    private static JsonArray jsonArrayFromCollection(Collection<Integer> collection) {
        JsonArray jsonArray = new JsonArray();
        collection.stream().map(i -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("index", i);
            return jsonObject;
        }).forEach(jsonArray::add);

        return jsonArray;
    }

    private static int getIndex(JsonObject jsonObject) {
        return jsonObject.get("index").getAsInt();
    }
}