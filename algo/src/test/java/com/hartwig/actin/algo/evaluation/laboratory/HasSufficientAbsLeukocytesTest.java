package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.junit.Test;

public class HasSufficientAbsLeukocytesTest {

    @Test
    public void canEvaluate() {
        HasSufficientAbsLeukocytes function = new HasSufficientAbsLeukocytes(3.5);

        assertEquals(Evaluation.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        ImmutableLabValue.Builder leukocytes = LaboratoryTestUtil.builder().code(LabMeasurement.LEUKOCYTES_ABS.code());

        assertEquals(Evaluation.PASS, function.evaluate(LaboratoryTestUtil.withLabValue(leukocytes.value(6D).build())));
        assertEquals(Evaluation.FAIL, function.evaluate(LaboratoryTestUtil.withLabValue(leukocytes.value(2D).build())));
    }
}