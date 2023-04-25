package com.hartwig.actin.algo.evaluation.treatment;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.junit.Test;

public class HasHadLimitedTreatmentsWithCategoryOfTypesTest {

    @Test
    public void canEvaluate() {
        TreatmentCategory category = TreatmentCategory.TARGETED_THERAPY;
        List<String> types = Lists.newArrayList("anti-BRAF", "anti-KRAS");
        HasHadLimitedTreatmentsWithCategoryOfTypes function = new HasHadLimitedTreatmentsWithCategoryOfTypes(category, types, 1);

        // No treatments yet
        List<PriorTumorTreatment> treatments = Lists.newArrayList();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add wrong treatment category
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.IMMUNOTHERAPY).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add correct treatment category with wrong type
        treatments.add(TreatmentTestFactory.builder().addCategories(category).targetedType("some anti-EGFR").build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add correct treatment category with correct type
        treatments.add(TreatmentTestFactory.builder().addCategories(category).targetedType("some anti-BRAF").build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add correct treatment category without a type
        treatments.add(TreatmentTestFactory.builder().addCategories(category).build());
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add correct treatment category with another correct type
        treatments.add(TreatmentTestFactory.builder().addCategories(category).targetedType("some anti-KRAS").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));
    }

    @Test
    public void canEvaluateWithTrials() {
        TreatmentCategory category = TreatmentCategory.TARGETED_THERAPY;
        HasHadLimitedTreatmentsWithCategoryOfTypes function =
                new HasHadLimitedTreatmentsWithCategoryOfTypes(category, Lists.newArrayList(), 1);

        // No treatments yet
        List<PriorTumorTreatment> treatments = Lists.newArrayList();
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add one trial
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.TRIAL).build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add another trial
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.TRIAL).build());
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));
    }
}