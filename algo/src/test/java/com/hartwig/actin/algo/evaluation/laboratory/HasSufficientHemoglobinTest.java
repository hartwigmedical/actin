package com.hartwig.actin.algo.evaluation.laboratory;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.ImmutableLabValue;
import com.hartwig.actin.clinical.interpretation.LabMeasurement;

import org.junit.Test;

public class HasSufficientHemoglobinTest {

    @Test
    public void canEvaluate() {
        HasSufficientHemoglobin function = new HasSufficientHemoglobin(7.5, LabUnit.MMOL_PER_L);

        // No measurements done
        assertEquals(Evaluation.UNDETERMINED, function.evaluate(TestDataFactory.createMinimalTestPatientRecord()));

        ImmutableLabValue.Builder hemoglobin = LaboratoryTestUtil.builder().code(LabMeasurement.HEMOGLOBIN.code());

        // Standard evaluation
        assertEquals(Evaluation.PASS, function.evaluate(LaboratoryTestUtil.withLabValue(hemoglobin.unit("mmol/L").value(8.5).build())));
        assertEquals(Evaluation.FAIL, function.evaluate(LaboratoryTestUtil.withLabValue(hemoglobin.unit("mmol/L").value(6.5).build())));

        // Different unit
        assertEquals(Evaluation.PASS, function.evaluate(LaboratoryTestUtil.withLabValue(hemoglobin.unit("g/dL").value(12.2).build())));
        assertEquals(Evaluation.FAIL, function.evaluate(LaboratoryTestUtil.withLabValue(hemoglobin.unit("g/dL").value(8.2).build())));

        // No recognized unit
        assertEquals(Evaluation.UNDETERMINED,
                function.evaluate(LaboratoryTestUtil.withLabValue(hemoglobin.unit("not a unit").value(4.2).build())));

        // Works with other unit as well.
        HasSufficientHemoglobin function2 = new HasSufficientHemoglobin(7.5, LabUnit.G_PER_DL);

        assertEquals(Evaluation.PASS, function2.evaluate(LaboratoryTestUtil.withLabValue(hemoglobin.unit("mmol/L").value(6.5).build())));

    }
}