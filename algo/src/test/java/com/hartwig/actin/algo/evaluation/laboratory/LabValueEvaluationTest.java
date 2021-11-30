package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.algo.datamodel.Evaluation;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class LabValueEvaluationTest {

    @Test
    public void canEvaluateOnMinValue() {
        assertEquals(Evaluation.PASS, LabValueEvaluation.evaluateOnMinimalValue(4D, Strings.EMPTY, 2D));
        assertEquals(Evaluation.FAIL, LabValueEvaluation.evaluateOnMinimalValue(1D, Strings.EMPTY, 2D));

        assertEquals(Evaluation.PASS, LabValueEvaluation.evaluateOnMinimalValue(4D, LabValueEvaluation.LARGER_THAN, 2D));
        assertEquals(Evaluation.FAIL, LabValueEvaluation.evaluateOnMinimalValue(1D, LabValueEvaluation.SMALLER_THAN, 2D));

        assertEquals(Evaluation.UNDETERMINED, LabValueEvaluation.evaluateOnMinimalValue(4D, LabValueEvaluation.SMALLER_THAN, 2D));
        assertEquals(Evaluation.UNDETERMINED, LabValueEvaluation.evaluateOnMinimalValue(1D, LabValueEvaluation.LARGER_THAN, 2D));
    }
}