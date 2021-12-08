package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.junit.Test;

public class HasLimitedCreatinineULNTest {

    @Test
    public void canEvaluate() {
        HasLimitedCreatinineULN function = new HasLimitedCreatinineULN(1.2);

        assertEquals(Evaluation.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        ImmutableLabValue.Builder creatinine = LaboratoryTestUtil.forMeasurement(LabMeasurement.CREATININE);

        assertEquals(Evaluation.PASS, function.evaluate(LaboratoryTestUtil.withLabValue(creatinine.value(80).refLimitUp(75D).build())));
        assertEquals(Evaluation.FAIL, function.evaluate(LaboratoryTestUtil.withLabValue(creatinine.value(100).refLimitUp(75D).build())));
    }
}