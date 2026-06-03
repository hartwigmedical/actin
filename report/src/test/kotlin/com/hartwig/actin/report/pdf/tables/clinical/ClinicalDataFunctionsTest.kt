package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.report.pdf.tables.clinical.ClinicalDataFunctions.toDateString
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ClinicalDataFunctionsTest {

    @Test
    fun `Should return null when year is null`() {
        assertThat(toDateString(null, 1, 1)).isNull()
    }

    @Test
    fun `Should return year when only year is provided`() {
        assertThat(toDateString(2024, null, null)).isEqualTo("2024")
    }

    @Test
    fun `Should return year-month when day is null`() {
        assertThat(toDateString(2024, 1, null)).isEqualTo("1/2024")
    }

    @Test
    fun `Should return year-month-day when all are provided`() {
        assertThat(toDateString(2024, 2, 1)).isEqualTo("1/2/2024")
    }

    @Test
    fun `Should return null when year is null and month and day are provided`() {
        assertThat(toDateString(null, 2, 1)).isNull()
    }

    @Test
    fun `Should return year only when year and day are provided and month is null`() {
        assertThat(toDateString(2024, null, 1)).isEqualTo("2024")
    }
}