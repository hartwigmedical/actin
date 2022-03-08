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

public class HasRecentlyReceivedCancerTherapyOfCategoryTest {

    @Test
    public void canEvaluate() {
        LocalDate minDate = LocalDate.of(2020, 6, 6);
        HasRecentlyReceivedCancerTherapyOfCategory function =
                new HasRecentlyReceivedCancerTherapyOfCategory(Sets.newHashSet("correct"), minDate);

        // Fail on no medications
        List<Medication> medications = Lists.newArrayList();
        assertFalse(function.isPass(WashoutTestFactory.withMedications(medications)));

        // Fail on medication with wrong category
        medications.add(WashoutTestFactory.builder().addCategories("other").stopDate(minDate.plusDays(1)).build());
        assertFalse(function.isPass(WashoutTestFactory.withMedications(medications)));

        // Fail on medication with old date
        medications.add(WashoutTestFactory.builder().addCategories("correct").stopDate(minDate.minusDays(1)).build());
        assertFalse(function.isPass(WashoutTestFactory.withMedications(medications)));

        // Pass on medication with recent date
        medications.add(WashoutTestFactory.builder().addCategories("correct").stopDate(minDate.plusDays(1)).build());
        assertTrue(function.isPass(WashoutTestFactory.withMedications(medications)));

        assertNotNull(function.passMessage());
        assertNotNull(function.failMessage());
    }
}