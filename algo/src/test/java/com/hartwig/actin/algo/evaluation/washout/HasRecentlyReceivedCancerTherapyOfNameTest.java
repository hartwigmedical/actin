package com.hartwig.actin.algo.evaluation.washout;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.medication.MedicationStatusInterpreter;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.junit.Test;

public class HasRecentlyReceivedCancerTherapyOfNameTest {

    @Test
    public void canEvaluate() {
        LocalDate minDate = LocalDate.of(2020, 6, 6);
        MedicationStatusInterpreter interpreter = WashoutTestFactory.activeFromDate(minDate);
        HasRecentlyReceivedCancerTherapyOfName function =
                new HasRecentlyReceivedCancerTherapyOfName(Sets.newHashSet("correct"), interpreter);

        // Fail on no medications
        List<Medication> medications = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(WashoutTestFactory.withMedications(medications)));

        // Fail on medication with wrong name
        medications.add(WashoutTestFactory.builder().name("other").stopDate(minDate.plusDays(1)).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(WashoutTestFactory.withMedications(medications)));

        // Fail on medication with old date
        medications.add(WashoutTestFactory.builder().name("correct").stopDate(minDate.minusDays(1)).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(WashoutTestFactory.withMedications(medications)));

        // Pass on medication with recent date
        medications.add(WashoutTestFactory.builder().name("correct").stopDate(minDate.plusDays(1)).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(WashoutTestFactory.withMedications(medications)));
    }
}