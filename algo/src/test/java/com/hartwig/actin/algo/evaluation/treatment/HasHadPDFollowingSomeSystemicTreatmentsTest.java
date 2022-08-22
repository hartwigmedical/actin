package com.hartwig.actin.algo.evaluation.treatment;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.junit.Test;

public class HasHadPDFollowingSomeSystemicTreatmentsTest {

    @Test
    public void canEvaluate() {
        HasHadPDFollowingSomeSystemicTreatments function = new HasHadPDFollowingSomeSystemicTreatments(1);

        // No treatments yet
        List<PriorTumorTreatment> treatments = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add one non-systemic
        treatments.add(TreatmentTestFactory.builder().isSystemic(false).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add one systemic with stop reason PD
        treatments.add(TreatmentTestFactory.builder()
                .name("treatment 1")
                .isSystemic(true)
                .startYear(2020)
                .stopReason(HasHadPDFollowingSomeSystemicTreatments.STOP_REASON_PD)
                .build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add a later systemic with other stop reason
        treatments.add(TreatmentTestFactory.builder().name("treatment 1").isSystemic(true).startYear(2021).stopReason("toxicity").build());
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));
    }

    @Test
    public void canEvaluateUninterruptedTreatments() {
        HasHadPDFollowingSomeSystemicTreatments function = new HasHadPDFollowingSomeSystemicTreatments(2);

        // No treatments yet
        List<PriorTumorTreatment> treatments = Lists.newArrayList();

        // Add 2 consecutive treatments
        treatments.add(TreatmentTestFactory.builder().isSystemic(true).name("treatment").startYear(2020).build());
        treatments.add(TreatmentTestFactory.builder().isSystemic(true).name("treatment").startYear(2021).build());
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));
    }
}