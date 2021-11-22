package com.hartwig.actin.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.junit.Test;

public class JsonTest {

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canExtractObjects() {
        JsonObject object = new JsonObject();

        object.add("object", new JsonObject());
        assertNotNull(Json.object(object, "object"));

        object.add("null", null);
        assertNull(Json.nullableObject(object, "null"));
    }

    @Test
    public void canExtractArrays() {
        JsonObject object = new JsonObject();

        object.add("array1", new JsonArray());
        assertNotNull(Json.array(object, "array1"));

        object.addProperty("nullable", (String) null);
        assertNull(Json.nullableStringList(object, "nullable"));

        JsonArray array = new JsonArray();
        array.add("value1");
        array.add("value2");
        object.add("array2", array);
        assertEquals(2, Json.nullableStringList(object, "array2").size());

        object.addProperty("string", "string");
        assertEquals(1, Json.nullableStringList(object, "string").size());
    }

    @Test
    public void canExtractStrings() {
        JsonObject object = new JsonObject();

        object.addProperty("nullable", (String) null);
        assertNull(Json.nullableString(object, "nullable"));

        object.addProperty("string", "value");
        assertEquals("value", Json.nullableString(object, "string"));
    }

    @Test
    public void canExtractNumbers() {
        JsonObject object = new JsonObject();

        object.addProperty("nullable", (String) null);
        assertNull(Json.nullableNumber(object, "nullable"));

        object.addProperty("number", 12.4);
        assertEquals(12.4, Json.nullableNumber(object, "number"), EPSILON);
    }

    @Test
    public void canExtractIntegers() {
        JsonObject object = new JsonObject();

        object.addProperty("nullable", (String) null);
        assertNull(Json.nullableInteger(object, "nullable"));

        object.addProperty("integer", 8);
        assertEquals(8, (int) Json.nullableInteger(object, "integer"));
    }

    @Test
    public void canExtractBooleans() {
        JsonObject object = new JsonObject();

        object.addProperty("nullable", (String) null);
        assertNull(Json.nullableBool(object, "nullable"));

        object.addProperty("bool", true);
        assertTrue(Json.nullableBool(object, "bool"));
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
    }
}