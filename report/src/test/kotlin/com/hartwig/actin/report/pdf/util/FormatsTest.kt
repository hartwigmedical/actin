package com.hartwig.actin.report.pdf.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FormatsTest {

    @Test
    fun `Should correctly format numbers to two digits`() {
        assertThat(Formats.twoDigitNumber(2.123)).isEqualTo("2.12")
        assertThat(Formats.twoDigitNumber(2.12)).isEqualTo("2.12")
        assertThat(Formats.twoDigitNumber(2.1)).isEqualTo("2.1")
        assertThat(Formats.twoDigitNumber(2.0)).isEqualTo("2")
    }

    @Test
    fun `Should force a single digit when required`() {
        assertThat(Formats.forcedSingleDigitNumber(2.123)).isEqualTo("2.1")
        assertThat(Formats.forcedSingleDigitNumber(2.12)).isEqualTo("2.1")
        assertThat(Formats.forcedSingleDigitNumber(2.1)).isEqualTo("2.1")
        assertThat(Formats.forcedSingleDigitNumber(2.0)).isEqualTo("2.0")
        assertThat(Formats.forcedSingleDigitNumber(0.0)).isEqualTo("0.0")
    }
}