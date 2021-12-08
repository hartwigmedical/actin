package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.junit.Test;

public class HasSufficientLabValueTest {

    @Test
    public void canEvaluate() {
        LabMeasurement measurement = LabMeasurement.THROMBOCYTES_ABS;
        HasSufficientLabValue function = new HasSufficientLabValue(measurement, 200D);

        assertEquals(Evaluation.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        ImmutableLabValue.Builder thrombocytes = LaboratoryTestUtil.forMeasurement(measurement);

        assertEquals(Evaluation.PASS, function.evaluate(LaboratoryTestUtil.withLabValue(thrombocytes.value(300D).build())));
        assertEquals(Evaluation.FAIL, function.evaluate(LaboratoryTestUtil.withLabValue(thrombocytes.value(100D).build())));
    }
}