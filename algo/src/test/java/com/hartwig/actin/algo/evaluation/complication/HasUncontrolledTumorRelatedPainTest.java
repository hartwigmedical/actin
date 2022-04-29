package com.hartwig.actin.algo.evaluation.complication;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.time.LocalDate;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.Complication;
import com.hartwig.actin.clinical.datamodel.ImmutableComplication;
import com.hartwig.actin.clinical.datamodel.ImmutableMedication;
import com.hartwig.actin.clinical.datamodel.Medication;
import com.hartwig.actin.clinical.datamodel.MedicationStatus;

import org.junit.Test;

public class HasUncontrolledTumorRelatedPainTest {

    private static final LocalDate TEST_DATE = LocalDate.of(2021, 4, 6);

    @Test
    public void canEvaluateOnComplication() {
        HasUncontrolledTumorRelatedPain function = new HasUncontrolledTumorRelatedPain(TEST_DATE);

        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplications(Lists.newArrayList())));

        Complication wrong = ImmutableComplication.builder().name("just a name").build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withComplication(wrong)));

        Complication match = ImmutableComplication.builder()
                .name("this is complication: " + HasUncontrolledTumorRelatedPain.SEVERE_PAIN_COMPLICATION)
                .build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ComplicationTestFactory.withComplication(match)));
    }

    @Test
    public void canEvaluateOnMedication() {
        HasUncontrolledTumorRelatedPain function = new HasUncontrolledTumorRelatedPain(TEST_DATE);

        Medication wrong = ImmutableMedication.builder().name("just some medication").build();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(ComplicationTestFactory.withMedication(wrong)));

        Medication match = ImmutableMedication.builder()
                .name(HasUncontrolledTumorRelatedPain.SEVERE_PAIN_MEDICATION)
                .status(MedicationStatus.ACTIVE)
                .startDate(TEST_DATE.minusDays(1))
                .build();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(ComplicationTestFactory.withMedication(match)));
    }
}