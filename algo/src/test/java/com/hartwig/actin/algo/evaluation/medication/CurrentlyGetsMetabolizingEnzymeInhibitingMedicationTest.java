package com.hartwig.actin.algo.evaluation.medication;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.junit.Test;

public class CurrentlyGetsMetabolizingEnzymeInhibitingMedicationTest {

    @Test
    public void canEvaluate() {
        CurrentlyGetsMetabolizingEnzymeInhibitingMedication function = new CurrentlyGetsMetabolizingEnzymeInhibitingMedication();

        // No medications yet
        List<Medication> medications = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)));

        // Various inactive medications
        medications.add(MedicationTestFactory.builder().active(false).build());
        medications.add(MedicationTestFactory.builder().active(null).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(MedicationTestFactory.withMedications(medications)));

        // One active medication
        medications.add(MedicationTestFactory.builder().active(true).build());
        assertEvaluation(EvaluationResult.WARN, function.evaluate(MedicationTestFactory.withMedications(medications)));
    }
}
