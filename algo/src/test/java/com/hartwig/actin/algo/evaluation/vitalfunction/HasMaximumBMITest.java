package com.hartwig.actin.algo.evaluation.vitalfunction;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;
import static org.junit.Assert.assertTrue;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableBodyWeight;
import org.junit.Test;

public class HasMaximumBMITest {

    private final HasMaximumBMI function = new HasMaximumBMI(40);
    private final LocalDate now = LocalDate.now();
    private final LocalDate lastYear = now.minusYears(1);

    @Test
    public void shouldBeUndeterminedWhenNoBodyWeightsProvided() {
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(VitalFunctionTestFactory.withBodyWeights(Collections.emptyList())));
    }

    @Test
    public void shouldBeUndeterminedWhenNoBodyWeightsProvidedWithExpectedUnit() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(VitalFunctionTestFactory.withBodyWeights(
                Collections.singletonList(ImmutableBodyWeight.builder().date(now).value(70).unit("pound").build())
        )));
    }

    @Test
    public void shouldPassIfLatestWeightIsLessThanWarnThreshold() {
        Evaluation evaluation = function.evaluate(VitalFunctionTestFactory.withBodyWeights(Arrays.asList(
                ImmutableBodyWeight.builder().date(now).value(70.57).unit("Kilogram").build(),
                ImmutableBodyWeight.builder().date(lastYear).value(100.0).unit("Kilogram").build()
        )));
        assertEvaluation(EvaluationResult.PASS, evaluation);
        assertTrue(evaluation.passSpecificMessages().contains(
                "Patient weight 70.6 kg will not exceed BMI limit of 40 for height >= 1.33 m"));
    }

    @Test
    public void shouldFailIfLatestWeightIsGreaterThanFailThreshold() {
        Evaluation evaluation = function.evaluate(VitalFunctionTestFactory.withBodyWeights(Arrays.asList(
                ImmutableBodyWeight.builder().date(now).value(180.32).unit("Kilogram").build(),
                ImmutableBodyWeight.builder().date(lastYear).value(100.0).unit("Kilogram").build()
        )));
        assertEvaluation(EvaluationResult.FAIL, evaluation);
        assertTrue(evaluation.failSpecificMessages().contains(
                "Patient weight 180.3 kg will exceed BMI limit of 40 for height < 2.12 m"));
    }

    @Test
    public void shouldWarnIfLatestWeightIsGreaterThanWarnThreshold() {
        Evaluation evaluation = function.evaluate(VitalFunctionTestFactory.withBodyWeights(Arrays.asList(
                ImmutableBodyWeight.builder().date(now).value(100.99).unit("Kilogram").build(),
                ImmutableBodyWeight.builder().date(lastYear).value(80.0).unit("Kilogram").build()
        )));
        assertEvaluation(EvaluationResult.WARN, evaluation);
        assertTrue(evaluation.warnSpecificMessages().contains(
                "Patient weight 101.0 kg will exceed BMI limit of 40 for height < 1.59 m"));
    }
}