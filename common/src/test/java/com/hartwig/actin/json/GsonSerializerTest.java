package com.hartwig.actin.json;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class GsonSerializerTest {

    @Test
    public void canCreateGsonSerializer() {
        assertNotNull(GsonSerializer.create());
    }
}