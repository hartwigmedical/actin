package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.algo.datamodel.Evaluation;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

public class LabValueEvaluationTest {

    @Test
    public void canEvaluateVersusMinValue() {
        assertEquals(Evaluation.PASS, LabValueEvaluation.evaluateVersusMinValue(4D, Strings.EMPTY, 2D));
        assertEquals(Evaluation.FAIL, LabValueEvaluation.evaluateVersusMinValue(1D, Strings.EMPTY, 2D));

        assertEquals(Evaluation.PASS, LabValueEvaluation.evaluateVersusMinValue(4D, LabValueEvaluation.LARGER_THAN, 2D));
        assertEquals(Evaluation.FAIL, LabValueEvaluation.evaluateVersusMinValue(1D, LabValueEvaluation.SMALLER_THAN, 2D));

        assertEquals(Evaluation.UNDETERMINED, LabValueEvaluation.evaluateVersusMinValue(4D, LabValueEvaluation.SMALLER_THAN, 2D));
        assertEquals(Evaluation.UNDETERMINED, LabValueEvaluation.evaluateVersusMinValue(1D, LabValueEvaluation.LARGER_THAN, 2D));
    }

    @Test
    public void canEvaluateVersusMaxValue() {
        assertEquals(Evaluation.PASS, LabValueEvaluation.evaluateVersusMaxValue(1D, Strings.EMPTY, 2D));
        assertEquals(Evaluation.FAIL, LabValueEvaluation.evaluateVersusMaxValue(4D, Strings.EMPTY, 2D));

        assertEquals(Evaluation.PASS, LabValueEvaluation.evaluateVersusMaxValue(1D, LabValueEvaluation.SMALLER_THAN, 2D));
        assertEquals(Evaluation.FAIL, LabValueEvaluation.evaluateVersusMaxValue(4D, LabValueEvaluation.LARGER_THAN, 2D));

        assertEquals(Evaluation.UNDETERMINED, LabValueEvaluation.evaluateVersusMaxValue(4D, LabValueEvaluation.SMALLER_THAN, 2D));
        assertEquals(Evaluation.UNDETERMINED, LabValueEvaluation.evaluateVersusMaxValue(1D, LabValueEvaluation.LARGER_THAN, 2D));
    }
}