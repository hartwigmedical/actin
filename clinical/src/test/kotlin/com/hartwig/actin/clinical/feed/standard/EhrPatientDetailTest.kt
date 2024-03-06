package com.hartwig.actin.clinical.feed.standard

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class EhrPatientDetailTest {

    @Test
    fun `Should return hex hash in base64`() {
        val ehrPatientDetail =
            EhrPatientDetail(1980, "MALE", LocalDate.of(2024, 2, 28), "f44f6e61b16fa450e32550acf578c3185d4b98ff0fa3a65bf34a589e806b5a0d")
        assertEquals("9E9uYbFvpFDjJVCs9XjDGF1LmP8Po6Zb80pYnoBrWg0=", ehrPatientDetail.hashedIdBase64())
    }
}