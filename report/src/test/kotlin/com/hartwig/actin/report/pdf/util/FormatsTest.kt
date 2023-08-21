package com.hartwig.actin.report.pdf.util

import com.hartwig.actin.report.pdf.util.Formats.twoDigitNumber
import org.junit.Assert
import org.junit.Test

class FormatsTest {
    @Test
    fun canFormatNumbers() {
        Assert.assertEquals("2.12", twoDigitNumber(2.123))
        Assert.assertEquals("2.12", twoDigitNumber(2.12))
        Assert.assertEquals("2.1", twoDigitNumber(2.1))
        Assert.assertEquals("2", twoDigitNumber(2.0))
    }
}