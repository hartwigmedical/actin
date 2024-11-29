package com.hartwig.actin.medication

import org.junit.Test
import org.assertj.core.api.Assertions.assertThat
import com.hartwig.actin.medication.MedicationInputChecker.isCyp
import com.hartwig.actin.medication.MedicationInputChecker.isTransporter

class MedicationInputCheckerTest {

    @Test
    fun `Should determine if string is CYP`() {
        assertThat(isCyp("3A4")).isTrue
        assertThat(isCyp("CYP3A4")).isFalse
        assertThat(isCyp("A4")).isFalse
        assertThat(isCyp("A4A")).isFalse
        assertThat(isCyp("3A")).isFalse
    }

    @Test
    fun `Should determine if string is transporter`() {
        assertThat(isTransporter("BCRP")).isTrue
        assertThat(isTransporter("PGP")).isTrue
        assertThat(isTransporter("BCRP1")).isFalse
        assertThat(isTransporter("PG")).isFalse
    }
}