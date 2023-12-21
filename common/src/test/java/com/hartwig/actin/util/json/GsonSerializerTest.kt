package com.hartwig.actin.util.json

import com.hartwig.actin.util.json.GsonSerializer.create
import org.junit.Assert
import org.junit.Test

class GsonSerializerTest {
    @Test
    fun canCreateGsonSerializer() {
        Assert.assertNotNull(create())
    }
}