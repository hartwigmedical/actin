package com.hartwig.actin.algo.evaluation.general;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import static org.junit.Assert.assertTrue;

import java.util.Collections;

import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasMaximumWHOStatusTest {

    private final HasMaximumWHOStatus function = new HasMaximumWHOStatus(2);

    @Test
    public void shouldReturnUndeterminedWhenWHOIsNull() {
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(GeneralTestFactory.withWHO(null)));
    }

    @Test
    public void shouldPassWhenWHOIsLessThanOrEqualToMaximum() {
        assertEvaluation(EvaluationResult.PASS, function.evaluate(GeneralTestFactory.withWHO(0)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(GeneralTestFactory.withWHO(1)));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(GeneralTestFactory.withWHO(2)));
    }

    @Test
    public void shouldFailWhenWHOIsGreaterThanMaximum() {
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(GeneralTestFactory.withWHO(3)));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(GeneralTestFactory.withWHO(4)));
    }

    @Test
    public void shouldWarnWhenWHOIsExactMatchAndPatientHasComplicationCategoriesOfConcern() {
        Evaluation evaluation =
                function.evaluate(GeneralTestFactory.withWHOAndComplications(2, Collections.singletonList("Pleural Effusions")));
        assertEvaluation(EvaluationResult.WARN, evaluation);
        assertTrue(evaluation.warnSpecificMessages()
                .contains(
                        "Patient WHO status 2 equals maximum but patient has complication categories of concern: " + "Pleural Effusions"));
    }
}