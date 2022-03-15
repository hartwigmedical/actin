package com.hartwig.actin.algo.evaluation.treatment;

import static com.hartwig.actin.algo.evaluation.EvaluationAssert.assertEvaluation;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.junit.Test;

public class HasHadTreatmentWithCategoryOfTypesTest {

    @Test
    public void canEvaluate() {
        HasHadTreatmentWithCategoryOfTypes function =
                new HasHadTreatmentWithCategoryOfTypes(TreatmentCategory.TARGETED_THERAPY, Lists.newArrayList("Anti-EGFR"));

        // No treatments yet
        List<PriorTumorTreatment> treatments = Lists.newArrayList();
        PatientRecord noPriorTreatmentRecord = TreatmentTestFactory.withPriorTumorTreatments(treatments);
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(noPriorTreatmentRecord));

        // Add one wrong category
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.IMMUNOTHERAPY).build());
        PatientRecord immunoRecord = TreatmentTestFactory.withPriorTumorTreatments(treatments);
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(immunoRecord));

        // Add one correct category but wrong type
        treatments.add(TreatmentTestFactory.builder().addCategories(TreatmentCategory.TARGETED_THERAPY).build());
        PatientRecord multiRecord1 = TreatmentTestFactory.withPriorTumorTreatments(treatments);
        assertEvaluation(EvaluationResult.FAIL, function.evaluate(multiRecord1));

        // Add one correct category with matching type
        treatments.add(TreatmentTestFactory.builder()
                .addCategories(TreatmentCategory.TARGETED_THERAPY)
                .targetedType("Some anti-EGFR Type")
                .build());
        PatientRecord multiRecord2 = TreatmentTestFactory.withPriorTumorTreatments(treatments);
        assertEvaluation(EvaluationResult.PASS, function.evaluate(multiRecord2));
    }
}