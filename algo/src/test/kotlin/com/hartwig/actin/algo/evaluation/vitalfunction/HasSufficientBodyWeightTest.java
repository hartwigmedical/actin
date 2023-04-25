package com.hartwig.actin.algo.evaluation.vitalfunction;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.BodyWeight;
import com.hartwig.actin.clinical.datamodel.ImmutableBodyWeight;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class HasSufficientBodyWeightTest {

    @Test
    public void canEvaluate() {
        LocalDate referenceDate = LocalDate.of(2020, 8, 20);
        HasSufficientBodyWeight function = new HasSufficientBodyWeight(60D);

        // No weights, cannot determine
        List<BodyWeight> weights = Lists.newArrayList();
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withBodyWeights(weights)));

        // Most recent too low
        weights.add(weight().date(referenceDate.minusDays(5)).value(50D).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(VitalFunctionTestFactory.withBodyWeights(weights)));

        // A later one does succeed
        weights.add(weight().date(referenceDate.minusDays(4)).value(70D).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(VitalFunctionTestFactory.withBodyWeights(weights)));

        // An even later one has wrong unit
        weights.add(weight().date(referenceDate.minusDays(3)).value(70D).unit("pounds").build());
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withBodyWeights(weights)));
    }

    @NotNull
    private static ImmutableBodyWeight.Builder weight() {
        return VitalFunctionTestFactory.bodyWeight().unit(HasSufficientBodyWeight.EXPECTED_UNIT);
    }
}