package com.hartwig.actin.medication

import com.hartwig.actin.datamodel.clinical.Dosage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DosageFormatterTest {

    @Test
    fun `Should format dosage range with if needed prefix`() {
        val dosage = Dosage(dosageMin = 10.0, dosageMax = 20.0, dosageUnit = "mg", ifNeeded = true)
        assertThat(DosageFormatter.formatDosage(dosage)).isEqualTo("if needed 10 - 20 mg")
    }

    @Test
    fun `Should format dosage as single value when minimum equals maximum`() {
        val dosage = Dosage(dosageMin = 1.234, dosageMax = 1.234, dosageUnit = "mg")
        assertThat(DosageFormatter.formatDosage(dosage)).isEqualTo("1.23 mg")
    }

    @Test
    fun `Should format unknown dosage limit as question mark`() {
        val dosage = Dosage(dosageMin = null, dosageMax = 2.0, dosageUnit = "mg")
        assertThat(DosageFormatter.formatDosage(dosage)).isEqualTo("? - 2 mg")
    }

    @Test
    fun `Should return unknown prescription when dosage unit is missing`() {
        val dosage = Dosage(dosageMin = 10.0, dosageMax = 20.0, dosageUnit = null)
        assertThat(DosageFormatter.formatDosage(dosage)).isEqualTo("unknown prescription")
    }

    @Test
    fun `Should return special dosage units verbatim`() {
        assertThat(DosageFormatter.formatDosage(Dosage(dosageUnit = "specific prescription"))).isEqualTo("specific prescription")
        assertThat(DosageFormatter.formatDosage(Dosage(dosageUnit = "unknown prescription"))).isEqualTo("unknown prescription")
    }

    @Test
    fun `Should format frequency with frequency unit`() {
        val dosage = Dosage(frequency = 2.5, frequencyUnit = "day")
        assertThat(DosageFormatter.formatFrequency(dosage)).isEqualTo("2.5 / day")
    }

    @Test
    fun `Should format unknown frequency as question mark when missing or zero`() {
        assertThat(DosageFormatter.formatFrequency(Dosage(frequency = null, frequencyUnit = "day"))).isEqualTo("? / day")
        assertThat(DosageFormatter.formatFrequency(Dosage(frequency = 0.0, frequencyUnit = "day"))).isEqualTo("? / day")
    }

    @Test
    fun `Should return special frequency units verbatim`() {
        assertThat(DosageFormatter.formatFrequency(Dosage(frequency = 2.0, frequencyUnit = "once"))).isEqualTo("once")
        assertThat(DosageFormatter.formatFrequency(Dosage(frequency = 2.0, frequencyUnit = "specific prescription")))
            .isEqualTo("specific prescription")
        assertThat(DosageFormatter.formatFrequency(Dosage(frequency = 2.0, frequencyUnit = "unknown prescription")))
            .isEqualTo("unknown prescription")
    }

    @Test
    fun `Should format period based frequency when period between unit is provided`() {
        val dosage = Dosage(frequency = 1.25, frequencyUnit = "day", periodBetweenValue = 6.0, periodBetweenUnit = "days")
        assertThat(DosageFormatter.formatFrequency(dosage)).isEqualTo("1.25 / 7 days")
    }

    @Test
    fun `Should return unknown prescription when frequency unit is missing`() {
        val dosage = Dosage(frequency = 2.0, frequencyUnit = null)
        assertThat(DosageFormatter.formatFrequency(dosage)).isEqualTo("unknown prescription")
    }
}
