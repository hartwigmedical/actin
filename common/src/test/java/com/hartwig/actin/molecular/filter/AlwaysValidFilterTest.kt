package com.hartwig.actin.molecular.filter

import org.junit.Assert
import org.junit.Test

class AlwaysValidFilterTest {
    @get:Test
    val isAlwaysValid: Unit
        get() {
            val filter = AlwaysValidFilter()
            Assert.assertTrue(filter.include("not a gene"))
        }
}