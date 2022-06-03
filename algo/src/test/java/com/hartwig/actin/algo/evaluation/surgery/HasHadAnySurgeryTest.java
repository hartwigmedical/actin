package com.hartwig.actin.algo.evaluation.surgery;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableSurgery;
import com.hartwig.actin.clinical.datamodel.Surgery;

import org.junit.Test;

public class HasHadAnySurgeryTest {

    @Test
    public void canEvaluate() {
        LocalDate minDate = LocalDate.of(2019, 8, 20);
        HasHadAnySurgery function = new HasHadAnySurgery(minDate);

        List<Surgery> surgeries = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(SurgeryTestFactory.withSurgeries(surgeries)));

        surgeries.add(ImmutableSurgery.builder().endDate(LocalDate.of(2010, 10, 10)).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(SurgeryTestFactory.withSurgeries(surgeries)));

        surgeries.add(ImmutableSurgery.builder().endDate(minDate).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(SurgeryTestFactory.withSurgeries(surgeries)));
    }
}