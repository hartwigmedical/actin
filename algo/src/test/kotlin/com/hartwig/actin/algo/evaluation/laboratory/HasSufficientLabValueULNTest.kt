package com.hartwig.actin.algo.evaluation.laboratory;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasSufficientLabValueULNTest {

    @Test
    public void canEvaluate() {
        HasSufficientLabValueULN function = new HasSufficientLabValueULN(0.5);

        PatientRecord record = TestDataFactory.createMinimalTestPatientRecord();

        assertEvaluation(EvaluationResult.PASS, function.evaluate(record, LabTestFactory.builder().value(120D).refLimitUp(200D).build()));
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record, LabTestFactory.builder().value(80D).build()));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record, LabTestFactory.builder().value(50D).refLimitUp(150D).build()));
    }
}