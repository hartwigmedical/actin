package com.hartwig.actin.util.json

import com.google.common.collect.Maps
import com.google.gson.JsonObject
import org.junit.Assert
import org.junit.Test

class JsonDatamodelCheckerTest {
    @Test
    fun canCheckExistenceOfFields() {
        val map: MutableMap<String, Boolean> = Maps.newHashMap()
        map["A"] = true
        map["B"] = false
        val checker = JsonDatamodelChecker("test", map)
        val `object` = JsonObject()
        Assert.assertFalse(checker.check(`object`))
        `object`.addProperty("A", "test A")
        Assert.assertTrue(checker.check(`object`))
        `object`.addProperty("B", "test B")
        Assert.assertTrue(checker.check(`object`))
        `object`.addProperty("C", "test C")
        Assert.assertFalse(checker.check(`object`))
    }
}