package com.hartwig.actin.algo.evaluation.washout;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
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
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(WashoutTestFactory.withMedications(medications)));

        // Fail on medication with wrong category
        medications.add(WashoutTestFactory.builder().addCategories("other").stopDate(minDate.plusDays(1)).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(WashoutTestFactory.withMedications(medications)));

        // Fail on medication with old date
        medications.add(WashoutTestFactory.builder().addCategories("correct").stopDate(minDate.minusDays(1)).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(WashoutTestFactory.withMedications(medications)));

        // Pass on medication with recent date
        medications.add(WashoutTestFactory.builder().addCategories("correct").stopDate(minDate.plusDays(1)).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(WashoutTestFactory.withMedications(medications)));
    }
}