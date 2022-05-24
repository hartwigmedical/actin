package com.hartwig.actin.algo.evaluation.treatment;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.junit.Test;

public class HasHadSomeTreatmentsWithCategoryOfTypesTest {

    @Test
    public void canEvaluate() {
        TreatmentCategory category = TreatmentCategory.TARGETED_THERAPY;
        HasHadSomeTreatmentsWithCategoryOfTypes function =
                new HasHadSomeTreatmentsWithCategoryOfTypes(category, Lists.newArrayList("Anti-EGFR", "Anti-EGFR"), 2);

        // No treatments yet
        List<PriorTumorTreatment> treatments = Lists.newArrayList();
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add wrong treatment category
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.IMMUNOTHERAPY).build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add correct treatment category with wrong type
        treatments.add(TreatmentTestFactory.builder().addCategories(category).targetedType("some other type").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add another correct treatment category with right type
        treatments.add(TreatmentTestFactory.builder().addCategories(category).targetedType("Some anti-EGFR treatment").build());
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add another correct treatment category but with no type
        treatments.add(TreatmentTestFactory.builder().addCategories(category).build());
        assertEvaluation(EvaluationResult.UNDETERMINED, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));

        // Add another correct treatment category with right type
        treatments.add(TreatmentTestFactory.builder().addCategories(category).targetedType("Another anti-EGFR").build());
        assertEvaluation(EvaluationResult.PASS, function.evaluate(TreatmentTestFactory.withPriorTumorTreatments(treatments)));
    }
}