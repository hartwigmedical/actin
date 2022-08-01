package com.hartwig.actin.algo.evaluation.complication;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.medication.MedicationStatusInterpretation;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.ImmutableMedication;
import com.hartwig.actin.clinical.datamodel.Medication;

import org.junit.Test;

public class HasPotentialUncontrolledTumorRelatedPainTest {

    @Test
    public void canEvaluateOnComplication() {
        HasPotentialUncontrolledTumorRelatedPain function = new HasPotentialUncontrolledTumorRelatedPain(medication -> MedicationStatusInterpretation.ACTIVE);

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplications(Lists.newArrayList())));

        Complication wrong = ComplicationTestFactory.builder().name("just a name").build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplication(wrong)));

        Complication match = ComplicationTestFactory.builder()
                .name("this is complication: " + HasPotentialUncontrolledTumorRelatedPain.SEVERE_PAIN_COMPLICATION)
                .build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ComplicationTestFactory.withComplication(match)));
    }

    @Test
    public void canEvaluateOnMedication() {
        HasPotentialUncontrolledTumorRelatedPain function = new HasPotentialUncontrolledTumorRelatedPain(medication -> MedicationStatusInterpretation.ACTIVE);

        Medication wrong = ImmutableMedication.builder().name("just some medication").build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withMedication(wrong)));

        Medication match = ImmutableMedication.builder().name(HasPotentialUncontrolledTumorRelatedPain.SEVERE_PAIN_MEDICATION).build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ComplicationTestFactory.withMedication(match)));
    }
}