package com.hartwig.actin.algo.evaluation.treatment;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.junit.Test;

public class HasHadPDFollowingTreatmentWithCategoryOfTypesTest {

    @Test
    public void canEvaluate() {
        HasHadPDFollowingTreatmentWithCategoryOfTypes function =
                new HasHadPDFollowingTreatmentWithCategoryOfTypes(TreatmentCategory.CHEMOTHERAPY, Lists.newArrayList("type 1"));

        List<PriorTumorTreatment> treatments = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Wrong category
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.RADIOTHERAPY).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Right category and type but no PD
        treatments.add(TreatmentTestFactory.builder()
                .addCategories(TreatmentCategory.CHEMOTHERAPY)
                .chemoType("type 1")
                .stopReason("toxicity")
                .build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Right category and missing type
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).stopReason("toxicity").build());
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Right category and type and missing stop reason
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.CHEMOTHERAPY).chemoType("type 1").build());
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Right category, type and stop reason PD
        treatments.add(TreatmentTestFactory.builder()
                .addCategories(TreatmentCategory.CHEMOTHERAPY)
                .chemoType("type 1")
                .stopReason(HasHadPDFollowingTreatmentWithCategoryOfTypes.STOP_REASON_PD)
                .build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));
    }
}