package com.hartwig.actin.algo.evaluation.util;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class PassOrFailEvaluationFunctionTest {

    @Test
    public void canEvaluate() {
        PassOrFailEvaluationFunction pass = new PassOrFailEvaluationFunction(pass());
        assertEvaluation(EvaluationResult.PASS, pass.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        PassOrFailEvaluationFunction fail = new PassOrFailEvaluationFunction(fail());
        assertEvaluation(EvaluationResult.FAIL, fail.evaluate(TestDataFactory.createMinimalTestPatientRecord()));
    }

    @NotNull
    private static PassOrFailEvaluator pass() {
        return new PassOrFailEvaluator() {
            @Override
            public boolean isPass(@NotNull PatientRecord record) {
                return true;
            }

            @NotNull
            @Override
            public String passMessage() {
                return Strings.EMPTY;
            }

            @NotNull
            @Override
            public String failMessage() {
                return Strings.EMPTY;
            }
        };
    }

    @NotNull
    private static PassOrFailEvaluator fail() {
        return new PassOrFailEvaluator() {
            @Override
            public boolean isPass(@NotNull PatientRecord record) {
                return false;
            }

            @NotNull
            @Override
            public String passMessage() {
                return Strings.EMPTY;
            }

            @NotNull
            @Override
            public String failMessage() {
                return Strings.EMPTY;
            }
        };
    }


}