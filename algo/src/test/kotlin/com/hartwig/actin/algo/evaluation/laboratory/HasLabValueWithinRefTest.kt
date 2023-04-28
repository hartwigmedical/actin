package com.hartwig.actin.algo.evaluation.laboratory;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasLabValueWithinRefTest {

    @Test
    public void canEvaluate() {
        HasLabValueWithinRef function = new HasLabValueWithinRef();
        PatientRecord record = TestDataFactory.createMinimalTestPatientRecord();

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record, LabTestFactory.builder().isOutsideRef(null).build()));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record, LabTestFactory.builder().isOutsideRef(false).build()));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record, LabTestFactory.builder().isOutsideRef(true).build()));
    }
}