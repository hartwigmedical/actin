package com.hartwig.actin.report.pdf.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FormatsTest {

    @Test
    fun `Can correctly format numbers to two digits`() {
        assertThat(Formats.twoDigitNumber(2.123)).isEqualTo("2.12")
        assertThat(Formats.twoDigitNumber(2.12)).isEqualTo("2.12")
        assertThat(Formats.twoDigitNumber(2.1)).isEqualTo("2.1")
        assertThat(Formats.twoDigitNumber(2.0)).isEqualTo("2")
    }

    @Test
    fun `Can insert spaces around plus signs`() {
        assertThat(Formats.insertSpacesAroundPlus("nothing to be  done here")).isEqualTo("nothing to be  done here")

        assertThat(Formats.insertSpacesAroundPlus(" + ")).isEqualTo(" + ")
        assertThat(Formats.insertSpacesAroundPlus("t+t")).isEqualTo("t + t")
        assertThat(Formats.insertSpacesAroundPlus("1+1=2")).isEqualTo("1 + 1=2")
        assertThat(Formats.insertSpacesAroundPlus("1 + 1=2")).isEqualTo("1 + 1=2")
    }
}