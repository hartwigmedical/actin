package com.hartwig.actin.algo.calendar

import com.hartwig.actin.clinical.datamodel.PatientDetails
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate

class HistoricDateProviderTest {
    @Test
    fun neverCreateHistoricDateInTheFuture() {
        val currentDate = LocalDate.now()
        val yesterday = withRegistrationDate(currentDate.minusDays(1))
        val provider = HistoricDateProvider.fromPatientDetails(yesterday)
        Assert.assertTrue(provider.date().minusDays(1).isBefore(currentDate))
        Assert.assertFalse(provider.isLive)
    }

    companion object {
        private fun withRegistrationDate(date: LocalDate): PatientDetails {
            return TestClinicalFactory.createMinimalTestClinicalRecord().patient.copy(registrationDate = date)
        }
    }
}