package com.hartwig.actin.clinical

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PatientIdTest {

    @Test
    fun `Should add ACTIN prefix if not there `() {
        assertThat(PatientId.from("1234567890")).isEqualTo("ACTN1234567890")
    }

    @Test
    fun `Should remove dashes`() {
        assertThat(PatientId.from("ACTN-123-456-789")).isEqualTo("ACTN123456789")
    }

    @Test
    fun `Should pass through valid ACTIN id`() {
        assertThat(PatientId.from("ACTN1234567890")).isEqualTo("ACTN1234567890")
    }
}