package com.hartwig.actin.util.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.LocalDate;

import com.google.gson.Gson;

import org.junit.Test;

public class GsonLocalDateAdapterTest {

    @Test
    public void canSerializeLocalDate() {
        LocalDate date = LocalDate.of(2022, 10, 20);

        Gson serializer = GsonSerializer.create();

        assertEquals(date, serializer.fromJson(serializer.toJson(date), LocalDate.class));
        assertNull(serializer.fromJson(serializer.toJson((LocalDate) null), LocalDate.class));
    }
}