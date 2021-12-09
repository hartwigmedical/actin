package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.junit.Test;

public class HasLimitedLabValueULNTest {

    @Test
    public void canEvaluate() {
        LabMeasurement measurement = LabMeasurement.CREATININE;
        HasLimitedLabValueULN function = new HasLimitedLabValueULN(measurement, 1.2);

        assertEquals(Evaluation.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        ImmutableLabValue.Builder creatinine = LabTestFactory.forMeasurement(measurement).refLimitUp(75D);

        assertEquals(Evaluation.PASS, function.evaluate(LabTestFactory.withLabValue(creatinine.value(80).build())));
        assertEquals(Evaluation.FAIL, function.evaluate(LabTestFactory.withLabValue(creatinine.value(100).build())));
    }
}