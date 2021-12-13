package com.hartwig.actin.algo.evaluation.medication;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.junit.Test;

public class CurrentlyGetsMedicationWithTypeTest {

    @Test
    public void canEvaluate() {
        CurrentlyGetsMedicationWithType function = new CurrentlyGetsMedicationWithType(null);

        List<Medication> medications = Lists.newArrayList();
        assertEquals(Evaluation.FAIL, function.evaluate(MedicationTestUtil.withMedications(medications)));

        medications.add(MedicationTestUtil.builder().build());
        assertEquals(Evaluation.FAIL, function.evaluate(MedicationTestUtil.withMedications(medications)));

        medications.add(MedicationTestUtil.builder().active(false).build());
        assertEquals(Evaluation.FAIL, function.evaluate(MedicationTestUtil.withMedications(medications)));

        medications.add(MedicationTestUtil.builder().active(true).build());
        assertEquals(Evaluation.PASS, function.evaluate(MedicationTestUtil.withMedications(medications)));
    }

    @Test
    public void canSpecifyType() {
        CurrentlyGetsMedicationWithType function = new CurrentlyGetsMedicationWithType("type 1");
        List<Medication> medications = Lists.newArrayList();

        medications.add(MedicationTestUtil.builder().active(true).type("type 2").build());
        assertEquals(Evaluation.FAIL, function.evaluate(MedicationTestUtil.withMedications(medications)));

        medications.add(MedicationTestUtil.builder().active(true).type("type 1").build());
        assertEquals(Evaluation.PASS, function.evaluate(MedicationTestUtil.withMedications(medications)));
    }
}