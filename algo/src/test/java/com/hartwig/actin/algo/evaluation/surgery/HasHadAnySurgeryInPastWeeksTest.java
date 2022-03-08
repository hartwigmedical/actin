package com.hartwig.actin.algo.evaluation.surgery;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableSurgery;
import com.hartwig.actin.clinical.datamodel.Surgery;

import org.junit.Test;

public class HasHadAnySurgeryInPastWeeksTest {

    @Test
    public void canEvaluate() {
        LocalDate minDate = LocalDate.of(2020, 2, 20);
        HasHadSurgeryInPastWeeks function = new HasHadSurgeryInPastWeeks(minDate);

        List<Surgery> surgeries = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(SurgeryTestFactory.withSurgeries(surgeries)));

        surgeries.add(ImmutableSurgery.builder().endDate(minDate.minusWeeks(4)).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(SurgeryTestFactory.withSurgeries(surgeries)));

        surgeries.add(ImmutableSurgery.builder().endDate(minDate.plusWeeks(2)).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(SurgeryTestFactory.withSurgeries(surgeries)));
    }
}