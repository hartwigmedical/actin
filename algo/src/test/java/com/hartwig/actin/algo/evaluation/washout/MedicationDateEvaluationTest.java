package com.hartwig.actin.algo.evaluation.washout;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.junit.Test;

public class MedicationDateEvaluationTest {

    @Test
    public void canEvaluateMedicationDates() {
        LocalDate date = LocalDate.of(2022, 3, 8);

        assertTrue(MedicationDateEvaluation.hasBeenGivenAfterDate(WashoutTestFactory.builder().stopDate(date.plusDays(1)).build(), date));
        assertTrue(MedicationDateEvaluation.hasBeenGivenAfterDate(WashoutTestFactory.builder().stopDate(date).build(), date));
        assertTrue(MedicationDateEvaluation.hasBeenGivenAfterDate(WashoutTestFactory.builder().stopDate(null).build(), date));

        assertFalse(MedicationDateEvaluation.hasBeenGivenAfterDate(WashoutTestFactory.builder().stopDate(date.minusDays(1)).build(), date));
    }
}