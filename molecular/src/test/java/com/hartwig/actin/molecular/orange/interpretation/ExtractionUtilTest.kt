package com.hartwig.actin.molecular.orange.interpretation

import org.junit.Assert
import org.junit.Test

class ExtractionUtilTest {
    @Test
    fun canKeep3Digits() {
        Assert.assertEquals(3.0, ExtractionUtil.keep3Digits(3.0), EPSILON)
        Assert.assertEquals(3.123, ExtractionUtil.keep3Digits(3.123), EPSILON)
        Assert.assertEquals(3.123, ExtractionUtil.keep3Digits(3.123456789), EPSILON)
    }

    companion object {
        private const val EPSILON = 1.0E-10
    }
}