package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.junit.Test;

public class HasLabValueWithinRefTest {

    @Test
    public void canEvaluate() {
        LabMeasurement measurement = LabMeasurement.MAGNESIUM;
        HasLabValueWithinRef function = new HasLabValueWithinRef(measurement);

        assertEquals(Evaluation.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        ImmutableLabValue.Builder magnesium = LaboratoryTestUtil.forMeasurement(measurement);

        assertEquals(Evaluation.UNDETERMINED, function.evaluate(LaboratoryTestUtil.withLabValue(magnesium.build())));
        assertEquals(Evaluation.PASS, function.evaluate(LaboratoryTestUtil.withLabValue(magnesium.isOutsideRef(false).build())));
        assertEquals(Evaluation.FAIL, function.evaluate(LaboratoryTestUtil.withLabValue(magnesium.isOutsideRef(true).build())));
    }
}