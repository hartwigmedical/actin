package com.hartwig.actin.util.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.LocalDateTime;

import com.google.gson.Gson;

import org.junit.Test;

public class GsonLocalDateTimeAdapterTest {

    @Test
    public void shouldSerializeLocalDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2022, 10, 20, 11, 12, 13);

        Gson serializer = GsonSerializer.create();

        assertEquals(dateTime, serializer.fromJson(serializer.toJson(dateTime), LocalDateTime.class));
        assertNull(serializer.fromJson(serializer.toJson((LocalDateTime) null), LocalDateTime.class));
    }
}