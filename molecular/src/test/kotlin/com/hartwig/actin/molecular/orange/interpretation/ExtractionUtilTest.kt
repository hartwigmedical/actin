package com.hartwig.actin.molecular.orange.interpretation

import junit.framework.TestCase.assertEquals
import org.junit.Test

class ExtractionUtilTest {

    @Test
    fun `Should keep three digits`() {
        assertEquals(3.0, ExtractionUtil.keep3Digits(3.0), EPSILON)
        assertEquals(3.123, ExtractionUtil.keep3Digits(3.123), EPSILON)
        assertEquals(3.123, ExtractionUtil.keep3Digits(3.123456789), EPSILON)
    }

    companion object {
        private const val EPSILON = 1.0E-10
    }
}