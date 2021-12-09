package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.LabValue;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.junit.Test;

public class LabMeasurementEvaluatorTest {

    @Test
    public void canEvaluate() {
        LabMeasurement measurement = LabMeasurement.ALBUMIN;
        LabMeasurementEvaluator function = new LabMeasurementEvaluator(measurement, x -> Evaluation.PASS);

        assertEquals(Evaluation.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        LabValue wrongUnit = LaboratoryTestUtil.builder().code(measurement.code()).build();
        assertEquals(Evaluation.UNDETERMINED, function.evaluate(LaboratoryTestUtil.withLabValue(wrongUnit)));

        LabValue correct = LaboratoryTestUtil.forMeasurement(measurement).build();
        assertEquals(Evaluation.PASS, function.evaluate(LaboratoryTestUtil.withLabValue(correct)));
    }
}