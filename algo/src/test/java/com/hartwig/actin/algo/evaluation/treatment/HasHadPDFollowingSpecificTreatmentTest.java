package com.hartwig.actin.algo.evaluation.treatment;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.junit.Test;

public class HasHadPDFollowingSpecificTreatmentTest {

    @Test
    public void canEvaluate() {
        HasHadPDFollowingSpecificTreatment function =
                new HasHadPDFollowingSpecificTreatment(Sets.newHashSet("treatment 1", "treatment 2"), TreatmentCategory.CHEMOTHERAPY);

        List<PriorTumorTreatment> treatments = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Wrong category
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.RADIOTHERAPY).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Right category but no correct name
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).name("other").build());
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Right category and name but missing stop reason
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).name("treatment 1").build());
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Right category, name and stop reason PD
        treatments.add(TreatmentTestFactory.builder()
                .addCategories(TreatmentCategory.CHEMOTHERAPY)
                .name("treatment 1")
                .stopReason(HasHadPDFollowingTreatmentWithCategory.STOP_REASON_PD)
                .build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));
    }

    @Test
    public void canEvaluateStopReasons() {
        String treatment = "right treatment";
        HasHadPDFollowingSpecificTreatment function = new HasHadPDFollowingSpecificTreatment(Sets.newHashSet(treatment), null);

        List<PriorTumorTreatment> treatments = Lists.newArrayList();

        // Right category but different stop reason
        treatments.add(TreatmentTestFactory.builder().name(treatment).stopReason("toxicity").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Right category but no stop reason
        treatments.add(TreatmentTestFactory.builder().name(treatment).stopReason(null).build());
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Right category and right stop reason
        treatments.add(TreatmentTestFactory.builder()
                .name(treatment)
                .stopReason(HasHadPDFollowingSpecificTreatment.STOP_REASON_PD)
                .build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));
    }

    @Test
    public void canEvaluateWithTrials() {
        HasHadPDFollowingSpecificTreatment function = new HasHadPDFollowingSpecificTreatment(Sets.newHashSet("right treatment"), null);

        // Add trial
        assertEvaluation(EvaluationResult.UNDETERMINED,
                function.evaluate(TreatmentTestFactory.withPriorTumorTreatment(TreatmentTestFactory.builder()
                        .addCategories(TreatmentCategory.TRIAL)
                        .build())));
    }
}