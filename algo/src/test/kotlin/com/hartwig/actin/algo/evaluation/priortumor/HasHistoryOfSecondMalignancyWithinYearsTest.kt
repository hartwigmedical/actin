package com.hartwig.actin.algo.evaluation.priortumor;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.time.LocalDate;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;

import org.junit.Test;

public class HasHistoryOfSecondMalignancyWithinYearsTest {

    @Test
    public void canEvaluate() {
        LocalDate minDate = LocalDate.of(2019, 6, 20);
        HasHistoryOfSecondMalignancyWithinYears function = new HasHistoryOfSecondMalignancyWithinYears(minDate);

        // No history in case of no second primary
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimaries(Lists.newArrayList())));

        // One second primary but more than 3 years ago.
        PriorSecondPrimary tooOld = PriorTumorTestFactory.builder().lastTreatmentYear(2018).build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimary(tooOld)));

        // One Second primary same year, no month known.
        PriorSecondPrimary aroundCutoff = PriorTumorTestFactory.builder().lastTreatmentYear(2019).build();
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimary(aroundCutoff)));

        // One second primary but less than 3 years ago.
        PriorSecondPrimary notTooLongAgo = PriorTumorTestFactory.builder().lastTreatmentYear(2019).lastTreatmentMonth(9).build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimary(notTooLongAgo)));

        // One second primary but less than 4 years ago diagnosed
        PriorSecondPrimary diagnosedNotTooLongAgo = PriorTumorTestFactory.builder().diagnosedYear(2019).build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimary(diagnosedNotTooLongAgo)));

        // One second primary no dates available
        PriorSecondPrimary noDates = PriorTumorTestFactory.builder().build();
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(PriorTumorTestFactory.withPriorSecondPrimary(noDates)));
    }
}