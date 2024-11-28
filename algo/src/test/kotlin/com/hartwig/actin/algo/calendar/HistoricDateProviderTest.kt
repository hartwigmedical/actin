package com.hartwig.actin.algo.calendar

import com.hartwig.actin.datamodel.clinical.PatientDetails
import com.hartwig.actin.datamodel.clinical.TestClinicalFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class HistoricDateProviderTest {

    @Test
    fun neverCreateHistoricDateInTheFuture() {
        val currentDate = LocalDate.now()
        val yesterday = withRegistrationDate(currentDate.minusDays(1))
        val provider = HistoricDateProvider.fromPatientDetails(yesterday)
        assertThat(provider.date().minusDays(1).isBefore(currentDate)).isTrue()
        assertThat(provider.isLive).isFalse()
    }

    private fun withRegistrationDate(date: LocalDate): PatientDetails {
        return TestClinicalFactory.createMinimalTestClinicalRecord().patient.copy(registrationDate = date)
    }
}