package com.hartwig.actin.algo.evaluation.washout;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.junit.Test;

public class HasRecentlyReceivedCancerTherapyOfNameTest {

    @Test
    public void canEvaluate() {
        LocalDate minDate = LocalDate.of(2020, 6, 6);
        HasRecentlyReceivedCancerTherapyOfName function = new HasRecentlyReceivedCancerTherapyOfName(Sets.newHashSet("correct"), minDate);

        // Fail on no medications
        List<Medication> medications = Lists.newArrayList();
        assertFalse(function.isPass(WashoutTestFactory.withMedications(medications)));

        // Fail on medication with wrong name
        medications.add(WashoutTestFactory.builder().name("other").stopDate(minDate.plusDays(1)).build());
        assertFalse(function.isPass(WashoutTestFactory.withMedications(medications)));

        // Fail on medication with old date
        medications.add(WashoutTestFactory.builder().name("correct").stopDate(minDate.minusDays(1)).build());
        assertFalse(function.isPass(WashoutTestFactory.withMedications(medications)));

        // Pass on medication with recent date
        medications.add(WashoutTestFactory.builder().name("correct").stopDate(minDate.plusDays(1)).build());
        assertTrue(function.isPass(WashoutTestFactory.withMedications(medications)));

        assertNotNull(function.passMessage());
        assertNotNull(function.failMessage());
    }
}