package com.hartwig.actin.algo.evaluation.medication;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableMedication;
import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class CurrentlyGetsMedicationWithCategoryTest {

    @Test
    public void canEvaluate() {
        CurrentlyGetsMedicationWithCategory function = new CurrentlyGetsMedicationWithCategory(null, false);

        List<Medication> medications = Lists.newArrayList();
        assertEquals(EvaluationResult.FAIL, function.evaluate(withMedications(medications)));

        medications.add(builder().build());
        assertEquals(EvaluationResult.FAIL, function.evaluate(withMedications(medications)));

        medications.add(builder().active(false).build());
        assertEquals(EvaluationResult.FAIL, function.evaluate(withMedications(medications)));

        medications.add(builder().active(true).build());
        assertEquals(EvaluationResult.PASS, function.evaluate(withMedications(medications)));
    }

    @Test
    public void canSpecifyType() {
        CurrentlyGetsMedicationWithCategory function = new CurrentlyGetsMedicationWithCategory("category 1", false);
        List<Medication> medications = Lists.newArrayList();

        medications.add(builder().active(true).addCategories("category 2").build());
        assertEquals(EvaluationResult.FAIL, function.evaluate(withMedications(medications)));

        medications.add(builder().active(true).addCategories("category 1").build());
        assertEquals(EvaluationResult.PASS, function.evaluate(withMedications(medications)));
    }

    @Test
    public void canCheckForStableDosing() {
        CurrentlyGetsMedicationWithCategory function = new CurrentlyGetsMedicationWithCategory(null, true);

        List<Medication> medications = Lists.newArrayList();

        Medication randomDosing = builder().active(true)
                .dosageMin(1D)
                .dosageMax(2D)
                .dosageUnit("unit 1")
                .frequency(3D)
                .frequencyUnit("unit 2")
                .ifNeeded(false)
                .build();

        medications.add(randomDosing);
        assertEquals(EvaluationResult.PASS, function.evaluate(withMedications(medications)));

        medications.add(builder().from(randomDosing).frequencyUnit("some other unit").build());
        assertEquals(EvaluationResult.FAIL, function.evaluate(withMedications(medications)));

        // Also fail in case a dosing is combined with medication without dosing.
        assertEquals(EvaluationResult.FAIL, function.evaluate(withMedications(Lists.newArrayList(randomDosing, builder().active(true).build()))));
    }

    @NotNull
    private static PatientRecord withMedications(@NotNull List<Medication> medications) {
        return ImmutablePatientRecord.builder()
                .from(TestDataFactory.createMinimalTestPatientRecord())
                .clinical(ImmutableClinicalRecord.builder()
                        .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                        .medications(medications)
                        .build())
                .build();
    }

    @NotNull
    private static ImmutableMedication.Builder builder() {
        return ImmutableMedication.builder().name(Strings.EMPTY);
    }
}