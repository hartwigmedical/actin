package com.hartwig.actin.medication

import com.hartwig.actin.medication.MedicationInputChecker.isCyp
import com.hartwig.actin.medication.MedicationInputChecker.isTransporter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MedicationInputCheckerTest {

    @Test
    fun `Should determine if string is CYP`() {
        assertThat(isCyp("CYP3A4_5")).isTrue
        assertThat(isCyp("CYP2B6")).isTrue
        assertThat(isCyp("3A4")).isFalse
        assertThat(isCyp("CYP")).isFalse
        assertThat(isCyp("CYP3A4")).isFalse
    }

    @Test
    fun `Should determine if string is transporter`() {
        assertThat(isTransporter("BCRP")).isTrue
        assertThat(isTransporter("PGP")).isTrue
        assertThat(isTransporter("BCRP1")).isFalse
        assertThat(isTransporter("PG")).isFalse
    }
}