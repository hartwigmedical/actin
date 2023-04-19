package com.hartwig.actin.soc.calendar

import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutablePatientDetails
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate

class HistoricDateProviderTest {
    @Test
    fun neverCreateHistoricDateInTheFuture() {
        val currentDate = LocalDate.now()
        val yesterday = withRegistrationDate(currentDate.minusDays(1))
        val provider = HistoricDateProvider.fromClinical(yesterday)
        Assert.assertTrue(provider.date().minusDays(1).isBefore(currentDate))
        Assert.assertFalse(provider.isLive)
    }

    companion object {
        private fun withRegistrationDate(date: LocalDate): ClinicalRecord {
            val base = TestClinicalFactory.createMinimalTestClinicalRecord()
            return ImmutableClinicalRecord.builder()
                .from(base)
                .patient(ImmutablePatientDetails.builder().from(base.patient()).registrationDate(date).build())
                .build()
        }
    }
}