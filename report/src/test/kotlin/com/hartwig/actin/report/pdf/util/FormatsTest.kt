package com.hartwig.actin.report.pdf.util

import com.hartwig.actin.report.pdf.util.Formats.twoDigitNumber
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FormatsTest {
    @Test
    fun shouldCorrectlyFormatNumbers() {
        assertThat(twoDigitNumber(2.123)).isEqualTo("2.12")
        assertThat(twoDigitNumber(2.12)).isEqualTo("2.12")
        assertThat(twoDigitNumber(2.1)).isEqualTo("2.1")
        assertThat(twoDigitNumber(2.0)).isEqualTo("2")
    }
}