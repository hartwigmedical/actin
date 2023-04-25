package com.hartwig.actin.algo.calendar;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutablePatientDetails;
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HistoricDateProviderTest {

    @Test
    public void neverCreateHistoricDateInTheFuture() {
        LocalDate currentDate = LocalDate.now();
        ClinicalRecord yesterday = withRegistrationDate(currentDate.minusDays(1));

        HistoricDateProvider provider = HistoricDateProvider.fromClinical(yesterday);

        assertTrue(provider.date().minusDays(1).isBefore(currentDate));
        assertFalse(provider.isLive());
    }

    @NotNull
    private static ClinicalRecord withRegistrationDate(@NotNull LocalDate date) {
        ClinicalRecord base = TestClinicalFactory.createMinimalTestClinicalRecord();

        return ImmutableClinicalRecord.builder()
                .from(base)
                .patient(ImmutablePatientDetails.builder().from(base.patient()).registrationDate(date).build())
                .build();
    }
}