package com.hartwig.actin.algo.evaluation.laboratory;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;

import org.junit.Test;

public class HasLabValueOutsideRefLimitUpTest {

    @Test
    public void canEvaluate() {
        HasLabValueOutsideRefLimitUp function = new HasLabValueOutsideRefLimitUp();
        PatientRecord record = TestDataFactory.createMinimalTestPatientRecord();

        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(record, LabTestFactory.builder().value(5D).refLimitUp(null).build()));
        assertEvaluation(EvaluationResult.PASS, function.evaluate(record, LabTestFactory.builder().value(5D).refLimitUp(3D).build()));
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(record, LabTestFactory.builder().value(5D).refLimitUp(7D).build()));
    }
}