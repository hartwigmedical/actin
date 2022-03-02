package com.hartwig.actin.algo.evaluation.laboratory;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasSufficientLabValueLLNTest {

    @Test
    public void canEvaluate() {
        HasSufficientLabValueLLN function = new HasSufficientLabValueLLN(2);

        PatientRecord record = TestDataFactory.createMinimalTestPatientRecord();

        assertEvaluation(EvaluationResult.PASS, function.evaluate(record, LabTestFactory.builder().value(80D).refLimitLow(35D).build()));
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record, LabTestFactory.builder().value(80D).build()));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record, LabTestFactory.builder().value(100D).refLimitLow(75D).build()));
    }
}