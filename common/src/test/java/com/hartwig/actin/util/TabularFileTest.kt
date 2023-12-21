package com.hartwig.actin.util

import com.hartwig.actin.util.TabularFile.createFields
import org.junit.Assert
import org.junit.Test

class TabularFileTest {
    @Test
    fun canCreateFields() {
        val header = arrayOf("header0", "header1", "header2")
        val fields = createFields(header)
        Assert.assertEquals(0, (fields["header0"] as Int).toLong())
        Assert.assertEquals(1, (fields["header1"] as Int).toLong())
        Assert.assertEquals(2, (fields["header2"] as Int).toLong())
    }
}