package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.junit.Test;

public class HasSufficientLabValueLLNTest {

    @Test
    public void canEvaluate() {
        LabMeasurement measurement = LabMeasurement.LEUKOCYTES_ABS;
        HasSufficientLabValueLLN function = new HasSufficientLabValueLLN(measurement, 2);

        assertEquals(Evaluation.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        ImmutableLabValue.Builder leukocytes = LaboratoryTestUtil.forMeasurement(measurement);

        assertEquals(Evaluation.PASS, function.evaluate(LaboratoryTestUtil.withLabValue(leukocytes.value(80).refLimitLow(35D).build())));
        assertEquals(Evaluation.FAIL, function.evaluate(LaboratoryTestUtil.withLabValue(leukocytes.value(100).refLimitLow(75D).build())));
    }
}