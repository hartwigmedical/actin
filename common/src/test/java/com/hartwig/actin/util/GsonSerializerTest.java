package com.hartwig.actin.util;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class GsonSerializerTest {

    @Test
    public void canCreateGsonSerializer() {
        assertNotNull(GsonSerializer.create());
    }
}